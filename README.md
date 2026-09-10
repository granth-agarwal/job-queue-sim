# job-queue-sim

A dependency-free Java simulation of typed background job execution, retries, and structured logging.

## Overview

`job-queue-sim` models a compact background job pipeline with builder-based construction, polymorphic dispatch, retry limits, and failure handling. It is intended as a small reference implementation for typed job dispatch, retry policy, and thread-safe shared logging.

## Features

- Typed job hierarchy for email, report generation, data synchronization, and retryable work
- Fluent builder-based job construction with required-field and job-type validation
- Retry policy with a code-configurable maximum-attempt constant
- Thread-safe shared logging exercised by two concurrent worker threads
- Structured log formatting and summary generation
- Custom checked execution exceptions and unchecked configuration validation

## Requirements

- JDK 17 or newer
- No external dependencies or build tool

## Getting started

From the project root:

```sh
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out com.caeliusconsulting.jobqueuesim.Main
```

## Project structure

```text
src/main/java/com/caeliusconsulting/jobqueuesim/
├── Job.java                         Abstract job state and execution contract
├── EmailJob.java                    Email-specific execution
├── ReportGenerationJob.java         Report-specific execution
├── DataSyncJob.java                 Data-sync execution and simulated failure path
├── RetryableJob.java                Stateful retryable job implementation
├── Retryable.java                   Optional retry capability contract
├── Loggable.java                    Logging contract with a default separator
├── JobBuilder.java                  Fluent validated job construction
├── Worker.java                      Execution, failure handling, counters, and parallel demo
├── JobType.java                     Type-safe supported-job enum
├── LogFormatter.java                Single-threaded log and summary formatting
├── Main.java                        Executable end-to-end trace
├── exceptions/                      Checked execution/retry and unchecked config exceptions
└── appendix/                        Standalone interface, abstract-class, and String examples
```

## Sample output

```text
[Header] Job Queue Simulation
---
[Builder / this] Building three chained job configurations:
[Polymorphism] Running EmailJob through a Job reference:
[timestamp] JOB email-1 :: Sending email to email-1@example.test
[Checked Exception] Handled retry limit: Maximum retry attempts exceeded
[Unchecked Exception] Handled invalid config: Job id and type are required
[StringBuilder Summary] Jobs: email-1 | report-1 | sync-1 | retry-1
[Static Counter] Total attempts processed: 9
```

## Design notes

[SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) documents the architecture, constraints, edge cases, and prioritized next steps. [TOPIC_MAPPING.md](TOPIC_MAPPING.md) maps the implemented Java language features to their source locations and runtime trace.
