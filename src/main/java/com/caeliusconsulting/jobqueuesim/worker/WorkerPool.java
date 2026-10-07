package com.caeliusconsulting.jobqueuesim.worker;

import com.caeliusconsulting.jobqueuesim.repository.JobRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages the lifecycle of the shared thread pool and worker submission.
 *
 * Design:
 *   - Single fixed thread pool sized to availableProcessors()
 *   - One Worker per queue type submitted to the shared pool
 *   - Worker threads are non-daemon (default ThreadFactory behaviour)
 *   - Graceful shutdown: shutdown() → awaitTermination(30s) → shutdownNow()
 *
 * Syllabus: ExecutorService, fixed thread pool, graceful shutdown,
 *           availableProcessors(), non-daemon threads
 */
public class WorkerPool {

    /** Worker count defaults to the number of CPU logical cores. */
    private static final int WORKER_COUNT =
            Runtime.getRuntime().availableProcessors();

    private final ExecutorService executor;
    private final JobDispatcher   dispatcher;

    public WorkerPool(JobDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        // Fixed pool — not one thread per job, not one pool per queue
        this.executor   = Executors.newFixedThreadPool(WORKER_COUNT,
                r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(false);  // Non-daemon — JVM waits for workers to finish
                    return t;
                });
    }

    /**
     * Submits one Worker per queue type to the shared executor.
     *
     * Email, report, and data-sync workers all compete for threads from the
     * same pool. The fallback worker handles UNKNOWN-typed jobs.
     */
    public void start() {
        int cpus = WORKER_COUNT;
        System.out.println("[WorkerPool] Starting " + cpus + " threads (availableProcessors=" + cpus + ")");

        JobRepository repo = dispatcher.getRepository();

        // Submit one named worker per queue — they all share the same thread pool
        executor.submit(new Worker(dispatcher.getEmailQueue(),    repo, "worker-email"));
        executor.submit(new Worker(dispatcher.getReportQueue(),   repo, "worker-report"));
        executor.submit(new Worker(dispatcher.getDataSyncQueue(), repo, "worker-datasync"));
        executor.submit(new Worker(dispatcher.getFallbackQueue(), repo, "worker-fallback"));
    }

    /**
     * Gracefully shuts down the worker pool.
     *
     * 1. shutdown()         — no new tasks accepted; existing workers finish current job
     * 2. awaitTermination() — wait up to 30 seconds for workers to drain their queues
     * 3. shutdownNow()      — interrupt workers if they haven't stopped (sends InterruptedException)
     *
     * Syllabus: ExecutorService.shutdown(), awaitTermination(), shutdownNow()
     */
    public void shutdown() {
        System.out.println("[WorkerPool] Initiating graceful shutdown...");
        executor.shutdown();

        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println("[WorkerPool] Workers did not finish in 30s — forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("[WorkerPool] All workers stopped. " +
                           "Total jobs processed: " + Worker.totalJobsProcessed.get());
    }
}
