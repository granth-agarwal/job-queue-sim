package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;
import java.time.LocalTime;

public abstract class Job implements Loggable {
    private final String jobId;
    private final LocalTime createdAt;

    protected Job(String jobId) {
        this.jobId = jobId;
        this.createdAt = LocalTime.now();
    }

    protected String getJobId() {
        return jobId;
    }

    protected LocalTime getCreatedAt() {
        return createdAt;
    }

    public abstract void execute() throws JobExecutionException;

    public void logStart() {
        System.out.println(LogFormatter.formatJobLog(getJobId(), "STARTING", getCreatedAt()));
    }

    @Override
    public void log(String message) {
        System.out.println(LogFormatter.formatJobLog(
                getJobId(), message, LocalTime.now()));
    }
}
