package com.caeliusconsulting.jobqueuesim.domain;

import com.caeliusconsulting.jobqueuesim.exceptions.InvalidJobConfigException;
import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

public final class DataSyncJob extends Job {
    private final int transientFailures;

    public DataSyncJob(String jobId, int maxAttempts, int transientFailures) {
        super(jobId, maxAttempts);
        if (transientFailures < 0) {
            throw new InvalidJobConfigException("Transient failure count cannot be negative");
        }
        this.transientFailures = transientFailures;
    }

    @Override
    public JobType getType() {
        return JobType.DATA_SYNC;
    }

    @Override
    public void execute() throws JobExecutionException, InterruptedException {
        Thread.sleep(250);
        if (getAttemptCount() <= transientFailures) {
            throw new JobExecutionException("Data source temporarily unavailable", true);
        }
    }
}
