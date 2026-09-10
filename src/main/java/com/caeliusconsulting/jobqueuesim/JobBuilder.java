package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.InvalidJobConfigException;

/** Builds jobs while demonstrating this-based field assignment and chaining. */
public class JobBuilder {
    private String id;
    private String type;
    private int priority;
    /** Sets the identifier and returns this builder for chaining. */
    public JobBuilder setId(String id) {
        this.id = id;
        return this;
    }
    /** Sets the type and returns this builder for chaining. */
    public JobBuilder setType(String type) {
        this.type = type;
        return this;
    }
    /** Sets the illustrative priority and returns this builder for chaining. */
    public JobBuilder setPriority(int priority) {
        this.priority = priority;
        return this;
    }
    /** Validates configuration and creates the requested concrete job. */
    public Job build() {
        if (id == null || id.isBlank() || type == null || type.isBlank()
                || !JobType.isValidType(type)) {
            throw new InvalidJobConfigException("Job id and valid type are required");
        }
        if (JobType.EMAIL.equals(type)) {
            return new EmailJob(id);
        }
        if (JobType.REPORT.equals(type)) {
            return new ReportGenerationJob(id);
        }
        return new DataSyncJob(id);
    }
}
