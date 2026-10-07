package com.caeliusconsulting.jobqueuesim.jobs;

import com.caeliusconsulting.jobqueuesim.exceptions.InvalidJobConfigException;

/**
 * Fluent builder for creating typed Job instances.
 *
 * Usage:
 * <pre>
 *     Job job = new JobBuilder()
 *         .setId("email-1")
 *         .setType(JobType.EMAIL)
 *         .build();
 * </pre>
 *
 * Syllabus: Builder pattern, method chaining, this reference
 */
public class JobBuilder {

    private String id;
    private JobType type;

    /** Whether the job should simulate a failure during execution. */
    private boolean simulateFailure = false;

    public JobBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public JobBuilder setType(JobType type) {
        this.type = type;
        return this;
    }

    public JobBuilder simulateFailure(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
        return this;
    }

    /**
     * Builds and returns the appropriate Job subclass.
     *
     * @throws InvalidJobConfigException if id or type is missing
     */
    public Job build() {
        if (id == null || id.isBlank() || type == null) {
            throw new InvalidJobConfigException("Job id and type are required");
        }
        return switch (type) {
            case EMAIL     -> new EmailJob(id);
            case REPORT    -> new ReportGenerationJob(id);
            case DATA_SYNC -> new DataSyncJob(id, simulateFailure);
            case UNKNOWN   -> throw new InvalidJobConfigException(
                    "Cannot build a job with type UNKNOWN");
        };
    }
}
