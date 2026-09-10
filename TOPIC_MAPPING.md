# Topic Mapping

This guide maps each assigned Java topic to the code that implements it and the trace that demonstrates it.

## 0. Naming Convention

**Where it lives:** Naming is consistent across `src/main/java/com/caeliusconsulting/jobqueuesim/`: classes and interfaces use PascalCase (`JobBuilder`, `Retryable`), methods and fields use camelCase (`processJob`, `retryCount`), constants use UPPER_SNAKE_CASE (`MAX_RETRY_ATTEMPTS`), enum values use uppercase names (`DATA_SYNC`), and packages are lowercase.

**What it demonstrates:** The names distinguish types, behavior, mutable state, and constants without annotations or explanatory comments. The package path mirrors `com.caeliusconsulting.jobqueuesim` exactly.

**How to see it run:** `Main.java:16-22` uses the named builder methods and job-type constants. The `[Builder / this]` trace is followed by job-specific output carrying IDs such as `email-1`, `report-1`, and `sync-1`; naming itself is verified in source rather than by runtime behavior.

## 1. OOP Concepts and the 4 Pillars, Real-World Example

**Where it lives:** Encapsulation is in `Job.java:6-19`, where `jobId` and `createdAt` are private and exposed to subclasses through protected getters. Abstraction is the `Job` base class and its abstract `execute()` contract at `Job.java:5-22`. Inheritance is visible in `EmailJob.java:3`, `ReportGenerationJob.java:3`, `DataSyncJob.java:5`, and `RetryableJob.java:6`. Polymorphism is exercised by `Worker.processJob(Job)` at `Worker.java:10-25`.

**What it demonstrates:** Different background jobs share identity and logging behavior while owning distinct execution logic: email derives a recipient, reports calculate a section count, and data sync can succeed or fail. `Worker` only needs the common `Job` type, so adding a subtype does not require a subtype-specific processing method.

**How to see it run:** `Main.java:24-33` sends three `Job` references and one failing `DataSyncJob` through the same `processJob()` method. The three `[Polymorphism]` blocks produce `Sending email`, `Generating report`, and `Syncing data batch`; the next block produces a handled data-sync failure.

## 2. Keywords: static, final, this, super

**Where it lives:** `static` shared state is declared at `Worker.java:6-8`, and stateless static formatting methods are at `LogFormatter.java:9-24`. `final` fixes the retry ceiling at `RetryableJob.java:7`, the shared buffer reference at `Worker.java:8`, and job identity state at `Job.java:6-7`. `this` resolves field/parameter name clashes and returns the current builder from `JobBuilder.java:9-17`. `super` initializes every subclass through its `Job` constructor and deliberately invokes the parent implementation at `RetryableJob.java:16` in `execute()`.

**What it demonstrates:** The worker counter and buffer belong to the class because every worker shares them; the retry limit cannot be reassigned. Builder chaining uses the current object, while the retryable subtype reuses base startup logging instead of duplicating it.

**How to see it run:** `Main.java:16-22` is the `[Builder / this]` chain, `Main.java:35-48` is the `[Inheritance + Interface + super]` retry flow, and `Main.java:64-66` prints the shared static counter and buffer. `JOB retry-1 :: STARTING` proves the parent `logStart()` implementation ran.

## 3. Interface vs Abstract Class, Difference, Example Programs, Implementation with Inheritance

### Integrated pipeline example

**Where it lives:** `Job.java:5-32` is an abstract class that owns state and provides shared logging while requiring `execute()`. `Retryable.java:5-8` is a capability contract with no state. `RetryableJob.java:6-34` combines both by extending `Job` and implementing `Retryable`. `Loggable.java:3-8` is a second interface and includes the Java 8 default method `logSeparator()`.

**What it demonstrates:** The abstract class is used where subclasses need common state and implementation; the interface describes optional behavior that not every job supports. `RetryableJob` is the single pipeline example of interface implementation alongside class inheritance.

**How to see it run:** `Main.java:12-14` calls the `Loggable` default separator. `Main.java:35-48` executes and retries `RetryableJob`, producing startup, handled-failure, later-success, and retry-limit lines.

### Reference-only interface catalog

`appendix/InterfacePatterns.java` is not wired into `Main`. It contains five standalone patterns:

1. Basic contract: `Payable` and `Employee` (`lines 8-17`).
2. Two unrelated interfaces: `Printable`, `Storable`, and `Document` (`lines 19-26`).
3. Interface constant: `Bounded.MAX_ITEMS` and `Inventory` (`lines 28-31`).
4. Default method: `Greeting.greet()` and `ReviewerGreeting` (`lines 33-36`).
5. Generic interface: `Converter<T>` and `IntegerConverter` (`lines 38-41`).

### Reference-only abstract-class catalog

`appendix/AbstractClassPatterns.java` is not wired into `Main`. It contains five standalone patterns:

1. Partial implementation: `Partial` and `CompletePartial` (`lines 8-14`).
2. Abstract-class constructor: `ConstructorBase` and `NamedAction` (`lines 16-25`).
3. Template method: `TemplateProcessor` and `SyncProcessor` (`lines 27-33`).
4. Static plus abstract members: `StaticAbstractMix` and `MixedDetail` (`lines 35-41`).
5. Field-backed base class: `FieldBacked` and `Counter` (`lines 43-48`).

## 4. Strings: Constant Pool vs Heap, String Functions, StringBuffer vs StringBuilder

### String Constant Pool vs Heap

**Where it lives:** The standalone `appendix/StringMethodsDemo.java:8-24` selects either an interned runtime argument or a heap copy and compares it with the pooled `"pooled-value"` literal. Domain configuration remains separate in the `JobType` enum.

**What it demonstrates:** `==` answers whether two references are identical, while `.equals()` compares their characters. With no third command-line argument, the heap copy prints `false` for reference identity and `true` for value equality; passing `pooled-value` as the third argument interns it and changes the reference result at runtime.

**How to see it run:** Run `java -cp out com.caeliusconsulting.jobqueuesim.appendix.StringMethodsDemo` for the heap-copy result, then pass `Job report pooled-value` to see the interned-reference result. This study-only behavior is intentionally not wired into `Main`.

### String function coverage

**Where it lives:** `appendix/StringMethodsDemo.java:25-39` groups inspection (`length`, `charAt`, `substring`, `indexOf`), comparison (`equals`, `equalsIgnoreCase`, `compareTo`), transformation (`trim`, `toUpperCase`, `replace`, `concat`), and splitting/searching (`split`, `contains`, `isEmpty`). Inputs come from arguments and contrasting call sites at `lines 8-18`, so the comparisons are not tautologies. The full String method reference table lives in the presentation deck and is not duplicated here.

**What it demonstrates:** The appendix gives executable examples of the major String operation families, including matching and non-matching inputs.

**How to see it run:** This reference-only class has its own `main()` and can be run as `java -cp out com.caeliusconsulting.jobqueuesim.appendix.StringMethodsDemo`; it is intentionally not part of the core `Main` trace.

### StringBuilder vs StringBuffer

**Where it lives:** `LogFormatter.java:9-24` uses `StringBuilder` for local, single-threaded formatting and summary assembly. `Worker.java:7-21` uses one static `StringBuffer` because two worker threads append to shared mutable state; `runInParallel()` creates and joins those threads at `Worker.java:28-39`.

**What it demonstrates:** `StringBuilder` avoids synchronization where one call owns the builder. `StringBuffer` is appropriate for the shared worker log because its mutating operations are synchronized across the two real threads.

**How to see it run:** `Main.java:58-66` prints `[StringBuffer + Threads]`, a `[StringBuilder Summary]`, and the complete `[Shared StringBuffer]` log containing both parallel job IDs.

## 5. Exception Handling

**Where it lives:** `JobExecutionException` and `RetryLimitExceededException` are checked exceptions in `exceptions/`. `DataSyncJob.execute()` throws the former at `DataSyncJob.java:17-23`, `Worker.processJob()` catches it and always records completion at `Worker.java:10-25`, and `RetryableJob.retry()` throws the latter at `RetryableJob.java:23-29`. `InvalidJobConfigException` is unchecked because invalid builder configuration is a preventable caller error; `JobBuilder.build()` throws it at `JobBuilder.java:19-22`.

**What it demonstrates:** Recoverable execution and retry failures must be declared and handled, while invalid construction is represented as an unchecked programming/configuration error. The worker logs a clean message and completes its accounting in `finally` without printing a stack trace.

**How to see it run:** `Main.java:32-33` produces `Handled failure: Data synchronization failed`, `Main.java:38-48` produces `Handled retry limit`, and `Main.java:50-56` produces `Handled invalid config`. The final shared log proves failed attempts still reached the `finally` path.
