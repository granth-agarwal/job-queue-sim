package com.caeliusconsulting.jobqueuesim.worker;

import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.JobType;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Routes incoming jobs to the appropriate typed BlockingQueue.
 *
 * There are four queues:
 *   - emailQueue     → EMAIL jobs
 *   - reportQueue    → REPORT jobs
 *   - dataSyncQueue  → DATA_SYNC jobs
 *   - fallbackQueue  → UNKNOWN or unrecognised types
 *
 * The same shared ExecutorService pool processes all queues.
 * Job type determines queue routing, NOT thread count.
 *
 * Syllabus: BlockingQueue (LinkedBlockingQueue), routing without instanceof
 */
public class JobDispatcher {

    private final BlockingQueue<Job> emailQueue    = new LinkedBlockingQueue<>();
    private final BlockingQueue<Job> reportQueue   = new LinkedBlockingQueue<>();
    private final BlockingQueue<Job> dataSyncQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Job> fallbackQueue = new LinkedBlockingQueue<>();

    private final JobRepository repository;

    public JobDispatcher(JobRepository repository) {
        this.repository = repository;
    }

    /**
     * Routes a job to its type-specific queue.
     *
     * Uses job.getType() — no instanceof checks needed.
     * LinkedBlockingQueue.put() never blocks here because capacity is unbounded.
     */
    public void dispatch(Job job) {
        BlockingQueue<Job> target = switch (job.getType()) {
            case EMAIL     -> emailQueue;
            case REPORT    -> reportQueue;
            case DATA_SYNC -> dataSyncQueue;
            case UNKNOWN   -> fallbackQueue;
        };

        try {
            target.put(job);
            System.out.println("[Dispatcher] Routed job '" + job.getJobId() +
                               "' → " + job.getType() + " queue");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Dispatcher] Interrupted while dispatching job: " + job.getJobId());
        }
    }

    // ── Queue accessors for WorkerPool ───────────────────────────────────────

    public BlockingQueue<Job> getEmailQueue()    { return emailQueue; }
    public BlockingQueue<Job> getReportQueue()   { return reportQueue; }
    public BlockingQueue<Job> getDataSyncQueue() { return dataSyncQueue; }
    public BlockingQueue<Job> getFallbackQueue() { return fallbackQueue; }
    public JobRepository      getRepository()    { return repository; }
}
