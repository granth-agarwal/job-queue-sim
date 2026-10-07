package com.caeliusconsulting.jobqueuesim.repository;

import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.JobStatus;

public interface JobRepository {
    void create(Job job);
    boolean claim(Job job);
    void update(Job job);
    JobStatus findStatus(String jobId);
}
