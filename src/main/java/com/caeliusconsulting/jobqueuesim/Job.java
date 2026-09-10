package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

/** Defines shared state and behavior while leaving execution to each job type. */
public abstract class Job implements Loggable {
    private String jobId;
    private long createdAt;

    /** Creates a job with an encapsulated identifier and creation time. */
    protected Job(String jobId) {
        this.jobId = jobId;
        this.createdAt = System.currentTimeMillis();
    }

    protected String getJobId() {
        return jobId;
    }

    protected long getCreatedAt() {
        return createdAt;
    }

    /** Runs subtype-specific work and reports recoverable execution failures. */
    public abstract void execute() throws JobExecutionException;

    /** Prints the common start message through the shared formatter. */
    public void logStart() {
        System.out.println(LogFormatter.formatJobLog(getJobId(), "STARTING", getCreatedAt()));
    }

    /** Prints a timestamped message for this job. */
    @Override
    public void log(String message) {
        System.out.println(LogFormatter.formatJobLog(
                getJobId(), message, System.currentTimeMillis()));
    }
}
