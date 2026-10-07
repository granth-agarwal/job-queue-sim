package com.caeliusconsulting.jobqueuesim.jobs;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

import java.time.LocalDateTime;

public abstract class Job {
    private final String jobId;
    private final int maxAttempts;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private JobStatus status = JobStatus.QUEUED;
    private int attemptCount;
    private String errorMessage;

    protected Job(String jobId, int maxAttempts) {
        if (jobId == null || jobId.isBlank() || jobId.length() > 100) {
            throw new IllegalArgumentException("Job ID must contain 1 to 100 characters");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("Maximum attempts must be positive");
        }
        this.jobId = jobId;
        this.maxAttempts = maxAttempts;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public final String getJobId() { return jobId; }
    public final String getDisplayId() {
        return jobId.substring(jobId.lastIndexOf(':') + 1);
    }

    public final int getMaxAttempts() { return maxAttempts; }
    public final int getAttemptCount() { return attemptCount; }
    public final LocalDateTime getCreatedAt() { return createdAt; }
    public final LocalDateTime getUpdatedAt() { return updatedAt; }
    public final JobStatus getStatus() { return status; }
    public final String getErrorMessage() { return errorMessage; }

    public final void beginAttempt() {
        requireStatus(JobStatus.QUEUED);
        if (attemptCount >= maxAttempts) {
            throw new IllegalStateException("No attempts remaining for " + jobId);
        }
        attemptCount++;
        transitionTo(JobStatus.PROCESSING, null);
    }

    public final void complete() {
        requireStatus(JobStatus.PROCESSING);
        transitionTo(JobStatus.COMPLETED, null);
    }

    public final boolean canRetry(JobExecutionException failure) {
        return failure.isTransient() && attemptCount < maxAttempts;
    }

    public final void requeue(String error) {
        requireStatus(JobStatus.PROCESSING);
        if (attemptCount >= maxAttempts) {
            throw new IllegalStateException("No attempts remaining for " + jobId);
        }
        transitionTo(JobStatus.QUEUED, error);
    }

    public final void fail(String error) {
        requireStatus(JobStatus.PROCESSING);
        transitionTo(JobStatus.FAILED, error);
    }

    private void requireStatus(JobStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Expected " + expected + " for " + jobId + ", was " + status);
        }
    }

    private void transitionTo(JobStatus next, String error) {
        status = next;
        errorMessage = error == null ? null : error.substring(0, Math.min(error.length(), 1000));
        updatedAt = LocalDateTime.now();
    }

    public abstract JobType getType();
    public abstract void execute() throws JobExecutionException, InterruptedException;
}
