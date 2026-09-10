# System Design

### 1. Overview

`job-queue-sim` is a small Java simulation of a background job pipeline. It constructs typed jobs, dispatches them through a common worker, demonstrates bounded retries, and records structured attempt logs.

It is not a production job queue. It has no persistence, durable message broker, scheduler, real email/report/sync integrations, network I/O, or long-running worker service; `Main` drives a finite in-memory demonstration.

### 2. Architecture

`Job` is the state-bearing abstract base. It encapsulates a job ID and creation timestamp, implements common logging, and requires each subtype to define `execute()`. `EmailJob`, `ReportGenerationJob`, and `DataSyncJob` provide distinct execution behavior, while `RetryableJob` adds stateful retry behavior.

`Loggable` stays separate because logging is a reusable capability and its default separator does not require `Job` state. `Retryable` stays separate because retry is optional: ordinary email, report, and data-sync jobs do not need `retry()` or a retry counter. `RetryableJob extends Job implements Retryable` combines shared job state with that optional contract.

The normal flow is:

```text
JobBuilder setters -> JobBuilder.build() -> concrete Job -> Worker.processJob()
                                                     -> Job.execute()
                                                     -> shared attempt log/counter
```

`JobBuilder` validates the required ID and type, then selects a subtype from `JobType`. `Worker.processJob()` invokes the subtype through the `Job` abstraction, catches checked execution failures, and records completion in `finally`.

`Worker.totalJobsProcessed` and `Worker.sharedLog` are static because all worker instances contribute to one process-wide attempt count and log. Counter increments are synchronized on `Worker.class`; the shared buffer is a `StringBuffer` so concurrent appends from `runInParallel()` are synchronized.

The root package contains the executable pipeline. `exceptions/` contains the two checked operational exceptions and one unchecked configuration exception. `appendix/` contains isolated interface, abstract-class, and String examples that are reference material rather than pipeline dependencies.

### 3. How to run it

Requirements: JDK 17 or newer. The project was built and tested with OpenJDK 26.0.2.1. It has no external dependencies and requires no build tool.

From a clean checkout:

```sh
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out com.caeliusconsulting.jobqueuesim.Main
```

### 4. What is actually covered

| Topic | Functional verification |
| --- | --- |
| Naming convention | Package, type, method, field, and constant names follow standard Java conventions and compile cleanly. |
| OOP and four pillars | Encapsulation, abstraction, inheritance, and polymorphic dispatch compile and produce distinct job output through one worker method. |
| `static`, `final`, `this`, `super` | Builder chaining, parent logging, the fixed retry ceiling, and shared worker state all execute in `Main`. |
| Interfaces and abstract classes | `RetryableJob` combines both forms; `Loggable`'s default method and the retry contract execute in the main trace. |
| Strings | Runtime-dependent pool/reference comparisons show both possible reference outcomes; StringBuilder and concurrent StringBuffer paths run successfully. |
| Exception handling | Checked execution/retry failures and unchecked invalid configuration are thrown, caught, and printed without raw stack traces. |

The complete source-to-topic trace is in [TOPIC_MAPPING.md](TOPIC_MAPPING.md).

### 5. Known lapses and simplifications — be specific and honest

- **No persistence:** Jobs, retry state, counters, and logs disappear when the process exits; a production implementation would store job payloads and state in a durable broker or database so work survives restarts.
- **No real scheduling or queueing:** `Main` calls `Worker.processJob()` directly, apart from the two-thread demonstration; a production implementation would use a blocking queue or a JMS/Kafka/Redis-backed consumer model with scheduled delivery.
- **No worker pool or backpressure:** `runInParallel()` creates two short-lived threads solely to exercise shared logging; a production implementation would use a bounded `ExecutorService`, queue capacity limits, rejection policy, and managed shutdown.
- **Immediate, externally driven retries:** `Main` calls `retry()` and `processJob()` synchronously with no delay; a production implementation would schedule retries with exponential backoff, jitter, and persisted attempt metadata.
- **No logging framework:** Output goes directly to `System.out`; a production implementation would emit structured logs through a logging facade with levels, context fields, and configurable sinks.
- **Compile-time configuration:** Job type strings and the retry ceiling are constants; a production implementation would validate typed configuration loaded from environment or service configuration and inject retry policy per job type.
- **No dead-letter or escalation path:** Execution failures are logged and processing continues; a production implementation would move permanently failed jobs to a dead-letter queue with diagnostics and alerting.
- **Shallow builder validation:** `JobBuilder` checks ID presence and known type but ignores priority range, ID format, and uniqueness; a production implementation would enforce a validation policy and reject duplicate idempotency keys at the persistence boundary.
- **Priority is stored but unused:** `setPriority()` records a value that does not affect dispatch order; a production implementation would put validated priority into the queued job envelope and use a priority-aware scheduler.
- **Direct constructors bypass builder validation:** Callers can construct jobs with invalid IDs; a production implementation would centralize validated creation in factories or enforce constructor invariants.
- **Static state has process-wide lifetime:** Repeated runs in the same JVM retain the counter and log; a production implementation would scope metrics and logging to injected services with explicit lifecycle/reset behavior.
- **Interruption handling is minimal:** `runInParallel()` restores the interrupt flag but may return before both threads finish if a join is interrupted; a production implementation would use managed futures and a defined cancellation/shutdown policy.

### 6. Edge cases — explicitly state coverage status for each

- **Covered — empty or blank job ID at build time:** `JobBuilder.build()` rejects `null`, empty, and whitespace-only IDs with `InvalidJobConfigException`; `Main` exercises the missing-field form of the same validation path.
- **Covered — unknown or invalid job type string at build time:** `JobType.isValidType()` returns false for unknown and null values, and `JobBuilder.build()` rejects them; `Main` visibly prints true for `EMAIL_JOB` and false for `UNKNOWN_JOB`.
- **Not covered — a job that fails every retry attempt through and beyond the limit:** `RetryableJob` is designed to succeed once `retryCount` reaches two, so permanent execution failure would require a separate always-failing retryable implementation and a retry coordinator test.
- **Covered — two jobs processed concurrently without shared-log corruption:** `Main` calls `Worker.runInParallel()`, both threads append complete lines through synchronized `StringBuffer.append()`, and both job IDs appear intact in the final shared log.
- **Covered — a job succeeding on a later retry attempt:** `Main` processes `RetryableJob` before retrying, sees failures below two attempts, then prints `Retryable work succeeded` after the second retry.
- **Covered by code inspection — calling `retry()` after the limit was exceeded:** Every later call increments `retryCount` again and rethrows because the value remains above three; the exception is consistent, but the count intentionally is not capped and this is not exercised in `Main`.
- **Not covered — very long or unusual job IDs:** IDs are not length-limited, normalized, or escaped, so they pass through to output verbatim and characters such as newlines could disrupt log formatting; production coverage would add constraints and escaping tests.
- **Not covered — null Strings outside the builder path:** Builder ID/type nulls are rejected, but direct job constructors accept null; behavior then varies by subtype, including `null@example.test` for email and a possible `NullPointerException` in report generation, so constructor-level validation is needed.
- **Covered by code inspection — calling `build()` twice on one builder:** The builder retains its fields and returns a new job with the same ID and type on each call; no mutable job state leaks between the two instances, but duplicate IDs are not prevented.
- **Covered — empty summary input:** `LogFormatter.buildSummary()` returns `Jobs: ` for an empty list without failing, although `Main` demonstrates only a populated list.
- **Not covered — null job or null summary list:** `Worker.processJob(null)` and `LogFormatter.buildSummary(null)` throw `NullPointerException`; a production API would either reject these explicitly with argument validation or document non-null contracts and test them.

### 7. If continued: prioritized next steps

1. Introduce an immutable job envelope with constructor-level validation, priority, attempt metadata, and an idempotency key.
2. Add a bounded queue and managed `ExecutorService` worker pool with backpressure and graceful shutdown.
3. Persist queued jobs and retry state through a durable broker or database, including dead-letter handling.
4. Move retry orchestration into the worker with configurable exponential backoff, jitter, and per-type policies.
5. Replace static output/state with injected structured logging, metrics, and lifecycle-managed storage.
6. Add automated tests for validation, retry ceilings, repeated retries, concurrency, interruption, and malformed identifiers.
