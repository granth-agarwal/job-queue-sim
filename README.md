# Job Queue Simulator

A Java 17 POC that processes nine simulated email, report, and data-sync jobs with reusable worker threads and JDBC persistence.

```text
Main (producer)
  ↓
Queue<Job> = LinkedList<Job> (synchronized access)
  ├── Thread worker-1 → Worker.run() → Job.execute() → JDBC / MySQL
  ├── Thread worker-2 → Worker.run() → Job.execute() → JDBC / MySQL
  └── Thread worker-N → Worker.run() → Job.execute() → JDBC / MySQL

All workers → ExecutionHistory (LinkedList protected by ReentrantLock)
```

`Main` persists jobs as `QUEUED` and adds them to one shared `Queue<Job>` backed by `LinkedList`. It creates `min(availableProcessors, jobs.size())` named workers using plain `Thread` and `Runnable`; an empty batch creates no workers. Workers reuse their threads to claim, execute, and persist jobs through the `Job` abstraction. The summary reports the actual worker count.

A transient failure returns the same job to the queue when attempts remain. Permanent failures and exhausted attempts become `FAILED`. The data-sync job `sync-003` fails once and then succeeds; jobs allow three total attempts. Mock execution uses short delays and never contacts external services.

Every queue `offer()` and `poll()` occurs inside `synchronized (queue)`. Workers use `wait()` when the queue is temporarily empty and wake when `Main` submits a job, a retry is queued, or the last job finishes. An `AtomicInteger` tracks unfinished jobs, including retries, so a worker does not mistake an empty queue for completion. `Main` waits with `Thread.join()`. Cleanup interrupts any workers still running after an error. Worker assignments and log ordering vary naturally.

Workers also record start, retry, completion, and failure events in one shared `LinkedList` execution history. A `ReentrantLock` protects that separate collection, and the final summary prints a snapshot of its events. Execution and JDBC calls happen outside the queue monitor.

MySQL stores job state and attempts; the in-memory execution history is printed for the current run. Each JDBC operation opens a short-lived `DriverManager` connection and closes its connection, prepared statement, and result set through try-with-resources. Conditional updates claim jobs atomically. A unique run prefix on job IDs keeps repeated runs from overwriting earlier records; logs show the readable part of each ID.

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
