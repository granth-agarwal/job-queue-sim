package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

/** Synchronizes data and supplies the deliberate checked-failure path. */
public class DataSyncJob extends Job {
    private boolean simulateFailure;

    /** Creates a data-sync job that succeeds by default. */
    public DataSyncJob(String jobId) {
        this(jobId, false);
    }

    /** Creates a data-sync job with an optional simulated failure. */
    public DataSyncJob(String jobId, boolean simulateFailure) {
        super(jobId);
        this.simulateFailure = simulateFailure;
    }

    /** Simulates data synchronization or throws the checked failure. */
    @Override
    public void execute() throws JobExecutionException {
        if (simulateFailure) {
            throw new JobExecutionException("Data synchronization failed for " + getJobId());
        }
        log("Syncing data");
    }
}
