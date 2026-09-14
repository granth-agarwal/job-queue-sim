package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

public class Worker {
    static int totalJobsProcessed = 0;
    // StringBuffer is synchronized because multiple worker threads mutate this shared buffer.
    static final StringBuffer sharedLog = new StringBuffer();

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

    public void runInParallel(Job job1, Job job2) {
        Thread first = new Thread(new Runnable() {
            @Override
            public void run() {
                processJob(job1);
            }
        }, "worker-1");
        Thread second = new Thread(new Runnable() {
            @Override
            public void run() {
                processJob(job2);
            }
        }, "worker-2");
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
