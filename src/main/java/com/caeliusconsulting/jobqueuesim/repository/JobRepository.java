package com.caeliusconsulting.jobqueuesim.repository;

import com.caeliusconsulting.jobqueuesim.domain.ExecutionSummary;
import com.caeliusconsulting.jobqueuesim.domain.Job;

import java.util.List;

public interface JobRepository {
    void create(Job job);
    boolean claim(Job job);
    void update(Job job);
    ExecutionSummary summarize(List<String> jobIds);
}
