package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

public class DataSyncJob extends Job {
    private boolean simulateFailure;

    public DataSyncJob(String jobId) {
        this(jobId, false);
    }

    public DataSyncJob(String jobId, boolean simulateFailure) {
        super(jobId);
        this.simulateFailure = simulateFailure;
    }

    @Override
    public void execute() throws JobExecutionException {
        if (simulateFailure) {
            throw new JobExecutionException("Data synchronization failed for " + getJobId());
        }
        long batchNumber = getCreatedAt().toSecondOfDay() % 1_000;
        log("Syncing data batch " + batchNumber);
    }
}
