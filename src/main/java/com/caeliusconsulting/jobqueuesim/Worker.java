package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

/** Executes jobs and owns process-wide worker statistics and log state. */
public class Worker {
    static int totalJobsProcessed = 0;
    // StringBuffer is used instead of StringBuilder because this buffer is shared
    // and mutated across worker threads; StringBuilder is not synchronized and is unsafe here.
    static final StringBuffer sharedLog = new StringBuffer();
    /** Executes one job while cleanly handling checked failures and final bookkeeping. */
    public void processJob(Job job) {
        try {
            job.execute();
        } catch (JobExecutionException exception) {
            sharedLog.append(LogFormatter.formatJobLog(job.getJobId(),
                    "FAILURE: " + exception.getMessage(), System.currentTimeMillis())
                    .concat(System.lineSeparator()));
            job.log("Handled failure: " + exception.getMessage());
        } finally {
            sharedLog.append(LogFormatter.formatJobLog(job.getJobId(),
                    "ATTEMPT COMPLETE", System.currentTimeMillis())
                    .concat(System.lineSeparator()));
            synchronized (Worker.class) {
                totalJobsProcessed++;
            }
        }
    }
    /** Processes two jobs on real threads and waits for both to finish. */
    public void runInParallel(Job job1, Job job2) {
        Thread first = new Thread(() -> processJob(job1), "worker-1");
        Thread second = new Thread(() -> processJob(job2), "worker-2");
        first.start();
        second.start();
        try {
            first.join();
            second.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.out.println("[Parallel] Interrupted while waiting for workers");
        }
    }
}
