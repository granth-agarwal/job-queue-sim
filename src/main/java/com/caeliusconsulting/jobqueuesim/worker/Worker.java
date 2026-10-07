package com.caeliusconsulting.jobqueuesim.worker;

import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.JobStatus;
import com.caeliusconsulting.jobqueuesim.jobs.LogFormatter;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Runnable worker that continuously drains jobs from a BlockingQueue.
 *
 * Lifecycle per job:
 *   claimJob() → PROCESSING → execute() → COMPLETED or FAILED → update DB
 *
 * Design decisions:
 *   - Workers are non-daemon threads (pool uses default factory).
 *   - Shared state (totalJobsProcessed) uses AtomicInteger — no explicit lock needed.
 *   - No shared StringBuilder — each worker prints its own log lines. Avoids
 *     the race conditions present in the original code.
 *   - Loop exits cleanly on InterruptedException (graceful shutdown signal).
 *   - DatabaseException during status update is logged but does not crash the worker.
 *
 * Syllabus: Runnable, BlockingQueue, AtomicInteger, thread safety, ExecutorService workers
 */
public class Worker implements Runnable {

    /**
     * Shared counter across all Worker instances.
     * AtomicInteger ensures increment is atomic without explicit synchronisation.
     *
     * Syllabus: AtomicInteger, thread-safe shared state
     */
    public static final AtomicInteger totalJobsProcessed = new AtomicInteger(0);

    private final BlockingQueue<Job> queue;
    private final JobRepository      repository;
    private final String             workerName;

    public Worker(BlockingQueue<Job> queue, JobRepository repository, String workerName) {
        this.queue      = queue;
        this.repository = repository;
        this.workerName = workerName;
    }

    /**
     * Main worker loop.
     *
     * BlockingQueue.take() blocks until a job is available, eliminating
     * busy-waiting. The loop exits when interrupted (shutdown signal from
     * ExecutorService.shutdownNow() or Thread.interrupt()).
     *
     * Syllabus: BlockingQueue.take(), InterruptedException, graceful shutdown
     */
    @Override
    public void run() {
        System.out.println("[" + workerName + "] started on thread: " +
                           Thread.currentThread().getName());

        while (!Thread.currentThread().isInterrupted()) {
            Job job = null;
            try {
                // Blocking call — waits until a job arrives
                job = queue.take();

                processJob(job);

            } catch (InterruptedException e) {
                // Restore interrupt flag and exit loop cleanly
                Thread.currentThread().interrupt();
                System.out.println("[" + workerName + "] interrupted — shutting down");
            }
        }

        System.out.println("[" + workerName + "] stopped.");
    }

    /**
     * Handles a single job: claim → execute → update status.
     *
     * Exception handling:
     *   - claimJob() returning false → another worker grabbed it first (skip)
     *   - JobExecutionException       → FAILED path
     *   - DatabaseException           → logged; job may be left in PROCESSING
     */
    private void processJob(Job job) {
        // Atomic claim — prevents two workers from double-processing
        boolean claimed = repository.claimJob(job.getJobId());
        if (!claimed) {
            System.out.println("[" + workerName + "] Job '" + job.getJobId() +
                               "' already claimed by another worker — skipping");
            return;
        }

        job.setStatus(JobStatus.PROCESSING);
        System.out.println(LogFormatter.formatJobLog(
                job.getJobId(), "PROCESSING started by " + workerName, LocalDateTime.now()));

        try {
            job.execute();

            // Success path
            job.setStatus(JobStatus.COMPLETED);
            repository.update(job);
            System.out.println(LogFormatter.formatJobLog(
                    job.getJobId(), "COMPLETED", LocalDateTime.now()));

        } catch (JobExecutionException e) {
            // Recoverable execution failure → FAILED
            job.setStatus(JobStatus.FAILED);
            try {
                repository.update(job);
            } catch (DatabaseException dbEx) {
                System.err.println("[" + workerName + "] Failed to persist FAILED status: "
                                   + dbEx.getMessage());
            }
            System.err.println(LogFormatter.formatJobLog(
                    job.getJobId(), "FAILED — " + e.getMessage(), LocalDateTime.now()));

        } catch (DatabaseException e) {
            System.err.println("[" + workerName + "] DB error while updating job '"
                               + job.getJobId() + "': " + e.getMessage());
        } finally {
            // AtomicInteger increment — thread-safe without synchronised block
            totalJobsProcessed.incrementAndGet();
        }
    }
}
