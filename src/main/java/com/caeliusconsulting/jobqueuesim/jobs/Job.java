package com.caeliusconsulting.jobqueuesim.jobs;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

import java.time.LocalDateTime;

/**
 * Abstract base class for all job types.
 *
 * Syllabus: Abstract classes, inheritance, polymorphism, interfaces,
 *           encapsulation, LocalDateTime
 *
 * Each concrete job:
 *  - inherits common state (id, type, status, timestamps)
 *  - implements execute() with its own logic
 *  - implements getType() for dispatcher routing (no instanceof needed)
 */
public abstract class Job implements Loggable {

    private final String jobId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private JobStatus status;

    protected Job(String jobId) {
        this.jobId      = jobId;
        this.createdAt  = LocalDateTime.now();
        this.updatedAt  = this.createdAt;
        this.status     = JobStatus.QUEUED;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String getJobId() {
        return jobId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status    = status;
        this.updatedAt = LocalDateTime.now();
    }

    /** Returns the concrete job type — used by the dispatcher for routing. */
    public abstract JobType getType();

    // ── Behaviour ────────────────────────────────────────────────────────────

    /**
     * Core execution logic implemented by each concrete subclass.
     * Throws JobExecutionException on recoverable execution failures.
     */
    public abstract void execute() throws JobExecutionException;

    /** Prints a START log line using the creation timestamp. */
    public void logStart() {
        System.out.println(LogFormatter.formatJobLog(jobId, "STARTING", createdAt));
    }

    /** Prints a log line tagged with the current time. */
    @Override
    public void log(String message) {
        System.out.println(LogFormatter.formatJobLog(jobId, message, LocalDateTime.now()));
    }

    @Override
    public String toString() {
        return "Job{id='" + jobId + "', type=" + getType() + ", status=" + status + "}";
    }
}
