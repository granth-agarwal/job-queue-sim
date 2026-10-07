# Job Queue Simulator — Java POC

A minimal, runnable Java proof-of-concept that demonstrates core Java and database syllabus
topics through the lens of a simple task-processing system — conceptually inspired by
Celery/RabbitMQ, but implemented with **plain Java and plain JDBC only**.

No Spring. No ORM. No Kafka. No Redis. No microservices.

---

## Architecture

```
Main
 │
 ├── Collections demos  (ArrayList, LinkedList, Stack, Queue, Tree)
 ├── Exception demos    (InvalidJobConfigException, RetryLimitExceededException)
 │
 └── Concurrent Job Pipeline
      │
      ├── JobBuilder → creates typed Job instances
      ├── JdbcJobRepository → persists jobs (status: QUEUED) via MySQL
      │
      ├── WorkerPool
      │    └── ExecutorService (fixed, availableProcessors() threads)
      │         └── Worker (Runnable) × 4 — one per queue type
      │
      ├── JobDispatcher → routes jobs to typed BlockingQueues
      │    ├── emailQueue     → LinkedBlockingQueue<Job>
      │    ├── reportQueue    → LinkedBlockingQueue<Job>
      │    ├── dataSyncQueue  → LinkedBlockingQueue<Job>
      │    └── fallbackQueue  → LinkedBlockingQueue<Job>
      │
      └── Job execution per worker:
           claimJob() → PROCESSING → execute() → COMPLETED or FAILED → DB update
```

---

## How Workers and Queues Work

1. **`JobDispatcher`** routes each job to a typed `LinkedBlockingQueue` based on `job.getType()`.
2. **`WorkerPool`** starts one `Worker` per queue type, all sharing a single `ExecutorService`
   fixed thread pool (size = `Runtime.getRuntime().availableProcessors()`).
3. Each **`Worker`** runs `BlockingQueue.take()` in a loop — it blocks until a job arrives,
   eliminating busy-waiting.
4. Before execution, the worker calls **`claimJob()`** which runs an atomic SQL:
   ```sql
   UPDATE jobs SET status = 'PROCESSING' WHERE id = ? AND status = 'QUEUED'
   ```
   If `affected rows == 0`, another worker already claimed it — the job is skipped.
5. On success: status → `COMPLETED`. On `JobExecutionException`: status → `FAILED`.
6. Workers are **non-daemon threads** — the JVM waits for them to finish.
7. **Graceful shutdown**: `executor.shutdown()` → `awaitTermination(30s)` → `shutdownNow()`.

---

## Syllabus Topic Mapping

| Topic | File / Class |
|---|---|
| Abstract class | [`Job`](src/main/java/com/caeliusconsulting/jobqueuesim/jobs/Job.java) |
| Inheritance / polymorphism | `EmailJob`, `ReportGenerationJob`, `DataSyncJob` |
| Builder pattern | [`JobBuilder`](src/main/java/com/caeliusconsulting/jobqueuesim/jobs/JobBuilder.java) |
| Interfaces | [`Loggable`](src/main/java/com/caeliusconsulting/jobqueuesim/jobs/Loggable.java), [`Retryable`](src/main/java/com/caeliusconsulting/jobqueuesim/jobs/Retryable.java), [`JobRepository`](src/main/java/com/caeliusconsulting/jobqueuesim/repository/JobRepository.java) |
| Enums | [`JobType`](src/main/java/com/caeliusconsulting/jobqueuesim/jobs/JobType.java), [`JobStatus`](src/main/java/com/caeliusconsulting/jobqueuesim/jobs/JobStatus.java) |
| Checked exceptions | `JobExecutionException`, `RetryLimitExceededException` |
| Unchecked exceptions | `InvalidJobConfigException`, `DatabaseException` |
| Exception wrapping / cause chain | [`DatabaseException`](src/main/java/com/caeliusconsulting/jobqueuesim/exceptions/DatabaseException.java) |
| StringBuilder (thread-local) | [`LogFormatter`](src/main/java/com/caeliusconsulting/jobqueuesim/jobs/LogFormatter.java) |
| `Runnable` / threads | [`Worker`](src/main/java/com/caeliusconsulting/jobqueuesim/worker/Worker.java) |
| `ExecutorService` | [`WorkerPool`](src/main/java/com/caeliusconsulting/jobqueuesim/worker/WorkerPool.java) |
| `BlockingQueue` | [`JobDispatcher`](src/main/java/com/caeliusconsulting/jobqueuesim/worker/JobDispatcher.java), `Worker` |
| `AtomicInteger` (thread-safe state) | `Worker.totalJobsProcessed` |
| Graceful shutdown | `WorkerPool.shutdown()` |
| `ArrayList` | [`ArrayListDemo`](src/main/java/com/caeliusconsulting/jobqueuesim/collections/ArrayListDemo.java) |
| `LinkedList` | [`LinkedListDemo`](src/main/java/com/caeliusconsulting/jobqueuesim/collections/LinkedListDemo.java) |
| `Stack` | [`StackDemo`](src/main/java/com/caeliusconsulting/jobqueuesim/collections/StackDemo.java) |
| `Queue` / `LinkedBlockingQueue` | [`QueueDemo`](src/main/java/com/caeliusconsulting/jobqueuesim/collections/QueueDemo.java) |
| `TreeSet` / `TreeMap` | [`TreeDemo`](src/main/java/com/caeliusconsulting/jobqueuesim/collections/TreeDemo.java) |
| JDBC `PreparedStatement` | [`JdbcJobRepository`](src/main/java/com/caeliusconsulting/jobqueuesim/repository/JdbcJobRepository.java) |
| `executeQuery()` (SELECT) | `JdbcJobRepository.findById()`, `findAll()` |
| `executeUpdate()` (INSERT/UPDATE/DELETE) | `JdbcJobRepository.create()`, `update()`, `delete()`, `claimJob()` |
| `execute()` (DDL + SELECT demo) | `JdbcJobRepository.initSchema()`, `demonstrateExecute()` |
| Try-with-resources | All `JdbcJobRepository` methods |
| Transactions (commit/rollback) | `JdbcJobRepository.create()` |
| Atomic DB claim | `JdbcJobRepository.claimJob()` |
| SQL basics | [`schema.sql`](src/main/resources/schema.sql) |
| Environment variable credentials | [`DatabaseConnection`](src/main/java/com/caeliusconsulting/jobqueuesim/database/DatabaseConnection.java) |

---

## MySQL Setup

```sql
-- Run once in your MySQL client:
CREATE DATABASE IF NOT EXISTS jobqueue;
```

The application creates the `jobs` table automatically on startup via `initSchema()`.

---

## Environment Variables

Copy `.env.example` to `.env` (the `.env` file is gitignored — never commit it):

```bash
cp .env.example .env
# Edit .env with your real credentials
```

Required variables:

| Variable | Example |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/jobqueue?useSSL=false&allowPublicKeyRetrieval=true` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | `your_password` |

---

## How to Build and Run

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.x running locally

### Build

```bash
mvn clean package -q
```

### Run (export env vars first)

```bash
export DB_URL="jdbc:mysql://localhost:3306/jobqueue?useSSL=false&allowPublicKeyRetrieval=true"
export DB_USER="root"
export DB_PASSWORD="your_password"

# Option A — fat jar
java -jar target/job-queue-sim-1.0.0.jar

# Option B — Maven exec plugin
mvn exec:java -Dexec.mainClass="com.caeliusconsulting.jobqueuesim.Main"
```

### Quick MySQL setup on macOS (Homebrew)

```bash
brew install mysql
brew services start mysql
mysql -u root -e "CREATE DATABASE IF NOT EXISTS jobqueue;"
```

---

## Expected Output (excerpt)

```
╔══════════════════════════════════════════════════╗
║         JOB QUEUE SIMULATOR  —  POC             ║
╚══════════════════════════════════════════════════╝

  SECTION 1: COLLECTIONS DEMOS
[ArrayList-1] Batch job collection: ...
[TreeMap-2]   Jobs grouped and sorted by type: ...

  SECTION 2: EXCEPTION DEMOS (Legacy)
[Exception] Caught InvalidJobConfigException: Job id and type are required

  SECTION 3: CONCURRENT JOB PIPELINE
[DB] Schema initialised. execute() returned: false
[WorkerPool] Starting 8 threads (availableProcessors=8)
[worker-email] started on thread: Thread-0
[worker-report] started on thread: Thread-1
[Dispatcher] Routed job 'email-1' → EMAIL queue
...
[19:45:01.234] [worker-email]    JOB email-1 :: COMPLETED
[19:45:01.251] [worker-datasync] JOB sync-fail-1 :: FAILED — Data sync failed

  Final Job Status Summary
  Job ID               Type            Status
  ──────────────────────────────────────────────────
  email-1              EMAIL           COMPLETED
  email-2              EMAIL           COMPLETED
  report-1             REPORT          COMPLETED
  sync-fail-1          DATA_SYNC       FAILED
  ...
```

---

## Package Structure

```
src/main/java/com/caeliusconsulting/jobqueuesim/
├── Main.java
├── jobs/
│   ├── Job.java               — abstract base class
│   ├── EmailJob.java
│   ├── ReportGenerationJob.java
│   ├── DataSyncJob.java
│   ├── JobBuilder.java        — builder pattern
│   ├── JobType.java           — enum (EMAIL, REPORT, DATA_SYNC, UNKNOWN)
│   ├── JobStatus.java         — enum (QUEUED, PROCESSING, COMPLETED, FAILED)
│   ├── Loggable.java          — interface with default method
│   ├── Retryable.java         — interface (standalone demo, not in pipeline)
│   ├── RetryableJob.java      — concrete demo class (not in pipeline)
│   └── LogFormatter.java      — StringBuilder-based log formatter
├── worker/
│   ├── Worker.java            — Runnable, BlockingQueue.take(), AtomicInteger
│   ├── WorkerPool.java        — ExecutorService, graceful shutdown
│   └── JobDispatcher.java     — routes to typed LinkedBlockingQueues
├── repository/
│   ├── JobRepository.java     — interface (CRUD + claimJob)
│   └── JdbcJobRepository.java — full JDBC implementation
├── database/
│   └── DatabaseConnection.java — DriverManager, env var credentials
├── collections/
│   ├── ArrayListDemo.java
│   ├── LinkedListDemo.java
│   ├── StackDemo.java
│   ├── QueueDemo.java
│   └── TreeDemo.java
└── exceptions/
    ├── JobExecutionException.java       — checked
    ├── InvalidJobConfigException.java   — unchecked
    ├── DatabaseException.java           — unchecked, wraps SQLException
    └── RetryLimitExceededException.java — checked
src/main/resources/
└── schema.sql  — SQL learning file (SELECT, WHERE, ORDER BY, COUNT, GROUP BY, ...)
```
