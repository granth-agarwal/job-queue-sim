package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.InvalidJobConfigException;

public class JobBuilder {
    private String id;
    private String type;
    private int priority;
    public JobBuilder setId(String id) {
        this.id = id;
        return this;
    }
    public JobBuilder setType(String type) {
        this.type = type;
        return this;
    }
    public JobBuilder setPriority(int priority) {
        this.priority = priority;
        return this;
    }
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
