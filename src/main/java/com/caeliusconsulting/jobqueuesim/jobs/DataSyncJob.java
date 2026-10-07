package com.caeliusconsulting.jobqueuesim.jobs;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

public final class DataSyncJob extends Job {
    private final int transientFailures;

    public DataSyncJob(String jobId, int maxAttempts, int transientFailures) {
        super(jobId, maxAttempts);
        if (transientFailures < 0) {
            throw new IllegalArgumentException("Transient failure count cannot be negative");
        }
        this.transientFailures = transientFailures;
    }

    @Override
    public JobType getType() {
        return JobType.DATA_SYNC;
    }

    @Override
    public void execute() throws JobExecutionException, InterruptedException {
        if (getAttemptCount() <= transientFailures) {
            Thread.sleep(80);
            throw new JobExecutionException("Data source temporarily unavailable", true);
        }
        Thread.sleep(250);
    }
}
