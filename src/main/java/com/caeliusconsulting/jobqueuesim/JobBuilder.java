package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.InvalidJobConfigException;

public class JobBuilder {
    private String id;
    private JobType type;

    public JobBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public JobBuilder setType(JobType type) {
        this.type = type;
        return this;
    }

    public Job build() {
        if (id == null || id.isBlank() || type == null) {
            throw new InvalidJobConfigException("Job id and type are required");
        }

        return switch (type) {
            case EMAIL -> new EmailJob(id);
            case REPORT -> new ReportGenerationJob(id);
            case DATA_SYNC -> new DataSyncJob(id);
        };
    }
}
