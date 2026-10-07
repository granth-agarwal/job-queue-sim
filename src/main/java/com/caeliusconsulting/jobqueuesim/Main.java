package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.collections.ArrayListDemo;
import com.caeliusconsulting.jobqueuesim.collections.LinkedListDemo;
import com.caeliusconsulting.jobqueuesim.collections.QueueDemo;
import com.caeliusconsulting.jobqueuesim.collections.StackDemo;
import com.caeliusconsulting.jobqueuesim.collections.TreeDemo;
import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.exceptions.InvalidJobConfigException;
import com.caeliusconsulting.jobqueuesim.exceptions.RetryLimitExceededException;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.JobBuilder;
import com.caeliusconsulting.jobqueuesim.jobs.JobStatus;
import com.caeliusconsulting.jobqueuesim.jobs.JobType;
import com.caeliusconsulting.jobqueuesim.jobs.LogFormatter;
import com.caeliusconsulting.jobqueuesim.jobs.RetryableJob;
import com.caeliusconsulting.jobqueuesim.repository.JdbcJobRepository;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;
import com.caeliusconsulting.jobqueuesim.worker.JobDispatcher;
import com.caeliusconsulting.jobqueuesim.worker.Worker;
import com.caeliusconsulting.jobqueuesim.worker.WorkerPool;

import java.util.List;


/**
 * Entry point and orchestrator for the Job Queue Simulator POC.
 *
 * This class does NOT contain:
 *   - SQL
 *   - Thread management logic
 *   - Business logic
 *
 * It delegates everything to the appropriate layer and calls the
 * collections / exception demos to satisfy the syllabus requirements.
 *
 * Execution flow:
 *   1. Collections demos (ArrayList, LinkedList, Stack, Queue, Tree)
 *   2. Exception demos (legacy RetryableJob, InvalidJobConfigException)
 *   3. DB schema init
 *   4. Job creation + persistence
 *   5. Worker pool start
 *   6. Job dispatch
 *   7. Wait for completion
 *   8. Final DB query + summary
 *   9. Graceful shutdown
 */
public final class Main {

    private Main() { }

    public static void main(String[] args) throws InterruptedException {
        printBanner();

        // ── 1. Collections demos ──────────────────────────────────────────────
        System.out.println("\n\n══════════════════════════════════════");
        System.out.println("  SECTION 1: COLLECTIONS DEMOS");
        System.out.println("══════════════════════════════════════");
        ArrayListDemo.runDemo();
        LinkedListDemo.runDemo();
        StackDemo.runDemo();
        QueueDemo.runDemo();
        TreeDemo.runDemo();

        // ── 2. Exception demos (legacy / standalone) ──────────────────────────
        System.out.println("\n\n══════════════════════════════════════");
        System.out.println("  SECTION 2: EXCEPTION DEMOS (Legacy)");
        System.out.println("══════════════════════════════════════");
        runExceptionDemos();

        // ── 3. Database + concurrent pipeline ────────────────────────────────
        System.out.println("\n\n══════════════════════════════════════");
        System.out.println("  SECTION 3: CONCURRENT JOB PIPELINE");
        System.out.println("══════════════════════════════════════");

        JobRepository repository = new JdbcJobRepository();

        try {
            runPipeline(repository);
        } catch (DatabaseException e) {
            System.err.println("\n[ERROR] Database error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("        Cause: " + e.getCause().getMessage());
            }
            System.err.println(
                "\n[HINT] Ensure the following environment variables are set:\n" +
                "       DB_URL      = jdbc:mysql://localhost:3306/jobqueue" +
                "?useSSL=false&allowPublicKeyRetrieval=true\n" +
                "       DB_USER     = <your_mysql_user>\n" +
                "       DB_PASSWORD = <your_mysql_password>\n" +
                "\n       Then run: CREATE DATABASE IF NOT EXISTS jobqueue;\n"
            );
            System.exit(1);
        }
    }

    // ── Pipeline orchestration ────────────────────────────────────────────────

    private static void runPipeline(JobRepository repository) throws InterruptedException {

        // 3a. Init schema — uses execute() (DDL)
        System.out.println("\n[Main] Initialising database schema...");
        repository.initSchema();

        // 3b. Demonstrate execute() on a SELECT (syllabus requirement)
        repository.demonstrateExecute();

        // 3c. Create jobs via JobBuilder
        System.out.println("\n[Main] Creating jobs...");
        List<Job> jobs = List.of(
            new JobBuilder().setId("email-1").setType(JobType.EMAIL).build(),
            new JobBuilder().setId("email-2").setType(JobType.EMAIL).build(),
            new JobBuilder().setId("report-1").setType(JobType.REPORT).build(),
            new JobBuilder().setId("report-2").setType(JobType.REPORT).build(),
            new JobBuilder().setId("sync-1").setType(JobType.DATA_SYNC).build(),
            new JobBuilder().setId("sync-2").setType(JobType.DATA_SYNC).build(),
            // This job will fail — demonstrates FAILED status path
            new JobBuilder().setId("sync-fail-1").setType(JobType.DATA_SYNC)
                            .simulateFailure(true).build()
        );

        // 3d. Persist each job (INSERT transaction + rollback demo)
        System.out.println("\n[Main] Persisting jobs to database...");
        for (Job job : jobs) {
            repository.create(job);
        }

        // 3e. Demonstrate findById and findAll
        System.out.println("\n[Main] Verifying persistence...");
        repository.findById("email-1").ifPresent(j ->
            System.out.println("[Main] findById('email-1'): " + j));

        List<Job> allQueued = repository.findAll();
        System.out.println("[Main] findAll() returned " + allQueued.size() + " jobs.");

        // 3f. Start worker pool
        JobDispatcher dispatcher = new JobDispatcher(repository);
        WorkerPool    workerPool = new WorkerPool(dispatcher);
        workerPool.start();

        // Brief pause to let workers fully start and print their start messages
        Thread.sleep(200);

        // 3g. Dispatch all jobs
        System.out.println("\n[Main] Dispatching jobs...");
        for (Job job : jobs) {
            dispatcher.dispatch(job);
        }

        // 3h. Wait for all jobs to complete (generous timeout for slow systems)
        System.out.println("\n[Main] Waiting for jobs to complete...");
        waitForCompletion(repository, jobs.size(), 15_000);

        // 3i. Final DB query + summary
        printFinalSummary(repository, jobs);

        // 3j. Demonstrate delete (CRUD completeness)
        System.out.println("\n[Main] Demonstrating DELETE — removing 'email-2'...");
        repository.delete("email-2");

        // 3k. Graceful shutdown
        System.out.println("\n[Main] Shutting down worker pool...");
        workerPool.shutdown();

        System.out.println("\n[Main] POC complete. Total jobs processed by workers: "
                           + Worker.totalJobsProcessed.get());
    }

    /**
     * Polls the DB until all jobs have left QUEUED/PROCESSING state,
     * or until the timeout elapses.
     */
    private static void waitForCompletion(JobRepository repository,
                                          int totalJobs,
                                          long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            List<Job> all = repository.findAll();
            long pending = all.stream()
                    .filter(j -> j.getStatus() == JobStatus.QUEUED
                              || j.getStatus() == JobStatus.PROCESSING)
                    .count();
            if (pending == 0) {
                System.out.println("[Main] All jobs finished.");
                return;
            }
            System.out.println("[Main] " + pending + " job(s) still pending...");
            Thread.sleep(500);
        }
        System.out.println("[Main] Timeout reached — some jobs may still be running.");
    }

    /** Queries all jobs and prints a formatted final status table. */
    private static void printFinalSummary(JobRepository repository, List<Job> dispatched) {
        System.out.println("\n[Main] ═══ Final Job Status Summary ═══");
        List<Job> all = repository.findAll();

        long completed = all.stream().filter(j -> j.getStatus() == JobStatus.COMPLETED).count();
        long failed    = all.stream().filter(j -> j.getStatus() == JobStatus.FAILED).count();
        long queued    = all.stream().filter(j -> j.getStatus() == JobStatus.QUEUED).count();

        System.out.printf("  %-20s %-15s %-12s%n", "Job ID", "Type", "Status");
        System.out.println("  " + "─".repeat(50));
        for (Job job : all) {
            System.out.printf("  %-20s %-15s %-12s%n",
                    job.getJobId(), job.getType(), job.getStatus());
        }
        System.out.println("  " + "─".repeat(50));
        System.out.printf("  COMPLETED: %d  |  FAILED: %d  |  QUEUED: %d%n",
                completed, failed, queued);

        List<String> ids = all.stream().map(Job::getJobId).toList();
        System.out.println("\n" + LogFormatter.buildSummary(ids));
    }

    // ── Exception demos ───────────────────────────────────────────────────────

    /**
     * Demonstrates the legacy OOP exception demos from the original codebase.
     * These run standalone — they are NOT wired into the concurrent pipeline.
     */
    private static void runExceptionDemos() {
        // InvalidJobConfigException (unchecked)
        System.out.println("\n[Exception] Building a job with no id or type:");
        try {
            new JobBuilder().build();
        } catch (InvalidJobConfigException e) {
            System.out.println("[Exception] Caught InvalidJobConfigException: " + e.getMessage());
        }

        // RetryableJob — checked exception demo (not in pipeline)
        System.out.println("\n[Exception] RetryableJob — standalone retry demo:");
        RetryableJob retryableJob = new RetryableJob("retry-demo-1");
        try {
            for (int attempt = 1; attempt <= RetryableJob.MAX_RETRY_ATTEMPTS + 1; attempt++) {
                retryableJob.retry();
                System.out.println("[Exception] Retry count: " + retryableJob.getRetryCount());
            }
        } catch (RetryLimitExceededException e) {
            System.out.println("[Exception] Caught RetryLimitExceededException: " + e.getMessage());
        }
    }

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║         JOB QUEUE SIMULATOR  —  POC              ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}