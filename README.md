# Job Queue Simulator

A small Java 17 application that processes a mixed batch of simulated email, report, and data-sync jobs. MySQL stores job states; a shared bounded `BlockingQueue<Job>` carries pending work to reusable workers.

```text
Main → submission → JDBC repository → MySQL
            ↓                ↑
     BlockingQueue<Job> → worker pool → Job.execute()
```

Startup loads configuration, opens the database pool, and creates the jobs table. It calculates worker count, starts that many consumers, persists each submitted job as `QUEUED`, and enqueues it. Workers atomically claim jobs as `PROCESSING`, execute them, and persist `COMPLETED`, `QUEUED` for a retry, or `FAILED`. After the batch finishes, the application queries its persisted summary, shuts down workers, and closes the database pool.

## Setup and run

Requires Java 17, Maven 3.8+, and MySQL 8+. Create a database and an application user with `CREATE`, `SELECT`, `INSERT`, and `UPDATE` permissions on it. For example, as a database administrator:

```sql
CREATE DATABASE jobqueue;
CREATE USER 'jobqueue'@'localhost' IDENTIFIED BY 'choose-a-local-password';
GRANT CREATE, SELECT, INSERT, UPDATE ON jobqueue.* TO 'jobqueue'@'localhost';
```

The application loads `src/main/resources/schema.sql` automatically. If upgrading an existing table from the original project, apply the following migration once as an administrator before starting the application:

```bash
mysql -u root -p jobqueue < db/migrate-v1.sql
```

Existing rows are retained. The application does not resume jobs left by earlier runs; this POC's database is a record of execution, not a durable scheduling queue. Each batch uses unique IDs, and summaries include only that batch. Log labels omit the run namespace for readability.

```bash
cp .env.example .env
# Edit .env with your database credentials.
set -a
source .env
set +a
mvn clean package
java -jar target/job-queue-sim-1.0.0.jar
```

`DB_URL` and `DB_USER` are required. `DB_PASSWORD` defaults to an empty string when omitted; an explicitly empty password is also accepted for a local database. In IntelliJ, a local database user with an empty password only needs `DB_URL` and `DB_USER` in the run configuration. The application reads environment variables; it does not load `.env` itself.

| Optional variable | Default | Purpose |
| --- | --- | --- |
| `QUEUE_CAPACITY` | `64` | Maximum unfinished jobs admitted at once |
| `MAX_JOB_ATTEMPTS` | `3` | Total attempts per job, including the first |
| `WORKLOAD_PROFILE` | `IO_BOUND` | `CPU_BOUND`, `IO_BOUND`, or `MIXED` |
| `WORKER_LIMIT` | `16` | Upper bound on worker threads |
| `DB_POOL_SIZE` | `4` | Independent upper bound on database connections |
| `BATCH_SIZE` | `9` | Number of jobs in the mixed demonstration batch |
| `BATCH_TIMEOUT_SECONDS` | `120` | Deadline covering submission and execution |

## Processing behavior

Worker sizing starts with processors available to the JVM. The policy suggests that count for CPU work, twice that count for I/O work, or about 1.5 times that count for mixed work. These are conservative POC heuristics, not universal sizing formulas. The result is capped by `WORKER_LIMIT`, batch size, and queue capacity. A batch of 1,000 jobs reuses the same bounded worker pool.

Admission uses a semaphore with the same capacity as the bounded queue. A permit is held until a job reaches a persisted terminal state, including across retries. This provides producer backpressure and guarantees space for a worker to requeue its job without deadlocking all consumers behind a full queue. Very small capacities also limit useful concurrency.

A transient execution failure returns the job to the shared queue when attempts remain. The attempt count increases when the next attempt starts, and any available worker can claim it. Permanent failures and exhausted attempts become `FAILED`. The third default job fails once and then succeeds deterministically; setting `MAX_JOB_ATTEMPTS=1` shows exhaustion. No real email, report, or external sync integration runs.

HikariCP manages the shared `DataSource`. Workers borrow JDBC connections only for short database operations and return them through try-with-resources before simulated work begins. Connection count is configured separately from worker count. SQL uses prepared statements and conditional updates; individual writes use auto-commit. Database failures abort the batch and retain their cause for diagnosis.

Normal shutdown lets consumers finish and exit through timed queue polling. Interruption is a bounded fallback. Job failures appear as concise warnings, and a successful default batch reports 9 completed jobs, 1 retry, and 10 attempts. Concurrent event ordering varies.

## Verification

`mvn clean verify` runs the concurrency, retry, lifecycle, and configuration tests. To include the JDBC integration tests, point these variables at a disposable MySQL database where the test user can create and drop tables:

```bash
export TEST_DB_URL='jdbc:mysql://localhost:3306/jobqueue_test?useSSL=false&allowPublicKeyRetrieval=true'
export TEST_DB_USER='your-test-user'
export TEST_DB_PASSWORD='your-test-password'
mvn clean verify
```

The JDBC tests replace the `jobs` table in that database to check fresh schema creation and the legacy migration. They are skipped when `TEST_DB_URL` is absent.

Original learning exercises remain under `examples/` and are excluded from the Maven application.
