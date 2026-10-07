# Job Queue Simulator

A Java 17 POC that processes nine simulated email, report, and data-sync jobs with three reusable worker threads and JDBC persistence.

```text
Main (producer)
  ↓
BlockingQueue<Job>
  ├── Thread worker-1 → Worker.run() → Job.execute()
  ├── Thread worker-2 → Worker.run() → Job.execute()
  └── Thread worker-3 → Worker.run() → Job.execute()
                            ↓
                    JdbcJobRepository → MySQL
```

`Main` persists jobs as `QUEUED` and puts them into one shared `ArrayBlockingQueue`. `Worker` implements `Runnable`. `Main` creates three named workers with `new Thread(worker, "worker-" + number)` and calls `start()` on each thread. These consumers repeatedly claim jobs, execute them through the `Job` abstraction, and persist the result. Logs show each worker starting, processing jobs, and stopping.

A transient failure returns the same job to the queue when attempts remain. Permanent failures and exhausted attempts become `FAILED`. The data-sync job `sync-003` fails once and then succeeds; jobs allow three total attempts. Mock execution uses short delays and never contacts external services.

The queue capacity equals the nine-job batch size, so retries always have room. An `AtomicInteger` tracks unfinished jobs, including retries. Workers poll the queue until that count reaches zero, and `Main` waits with `Thread.join()`. Normal completion lets workers exit on their own; cleanup interrupts any workers still running after an error. Worker assignments and log ordering vary naturally.

MySQL stores job state and execution history. Each JDBC operation opens a short-lived `DriverManager` connection and closes its connection, prepared statement, and result set through try-with-resources. Conditional updates claim jobs atomically. A unique run prefix on job IDs keeps repeated runs from overwriting earlier records; logs show the readable part of each ID.

## Setup

Requires Java 17, Maven 3.8+, and MySQL 8+. As a database administrator, create a database and application user:

```sql
CREATE DATABASE jobqueue;
CREATE USER 'jobqueue'@'localhost' IDENTIFIED BY 'choose-a-local-password';
GRANT CREATE, SELECT, INSERT, UPDATE ON jobqueue.* TO 'jobqueue'@'localhost';
```

The application creates the jobs table from `src/main/resources/schema.sql` on startup. An existing table must have the columns defined in that file. Jobs from earlier runs remain as history; the application processes only the current in-memory batch.

`DB_URL` and `DB_USER` are required environment variables. `DB_PASSWORD` defaults to an empty string when omitted. The application does not load `.env` itself.

## Build and run

```bash
cp .env.example .env
# Edit .env with your local database settings.
set -a
source .env
set +a
mvn clean package
java -jar target/job-queue-sim-1.0.0.jar
```

For an IntelliJ run configuration, set `DB_URL` and `DB_USER`, plus `DB_PASSWORD` if your user has a password, then run `Main`.

A successful run reports 9 completed jobs, 0 failed jobs, 1 retry, and 10 attempts. JDBC failures are reported with their cause and stop processing. Original collection exercises remain under `examples/` and are excluded from the application.
