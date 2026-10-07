package com.caeliusconsulting.jobqueuesim.jobs;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

/**
 * Synchronises a data batch.
 *
 * When {@code simulateFailure} is true the job throws a checked
 * {@link JobExecutionException}, triggering the FAILED path in the worker.
 *
 * Syllabus: Inheritance, checked exceptions, boolean flags
 */
public class DataSyncJob extends Job {

    private final boolean simulateFailure;

    public DataSyncJob(String jobId) {
        this(jobId, false);
    }

    public DataSyncJob(String jobId, boolean simulateFailure) {
        super(jobId);
        this.simulateFailure = simulateFailure;
    }

    @Override
    public JobType getType() {
        return JobType.DATA_SYNC;
    }

    @Override
    public void execute() throws JobExecutionException {
        logStart();
        if (simulateFailure) {
            simulateWork(80);
            throw new JobExecutionException(
                    "Data synchronisation failed for job: " + getJobId());
        }
        long batch = getCreatedAt().toLocalTime().toSecondOfDay() % 1_000;
        log("Syncing data batch #" + batch);
        simulateWork(250);
        log("Data sync complete for batch #" + batch);
    }

    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
