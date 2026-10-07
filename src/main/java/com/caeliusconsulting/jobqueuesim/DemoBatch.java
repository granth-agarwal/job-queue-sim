package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.domain.DataSyncJob;
import com.caeliusconsulting.jobqueuesim.domain.EmailJob;
import com.caeliusconsulting.jobqueuesim.domain.Job;
import com.caeliusconsulting.jobqueuesim.domain.ReportGenerationJob;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class DemoBatch {
    private DemoBatch() { }

    static List<Job> create(int size, int maxAttempts) {
        String runId = UUID.randomUUID().toString();
        List<Job> jobs = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            String sequence = String.format("%03d", index + 1);
            jobs.add(switch (index % 3) {
                case 0 -> new EmailJob(runId + ":email-" + sequence, maxAttempts);
                case 1 -> new ReportGenerationJob(runId + ":report-" + sequence, maxAttempts);
                default -> new DataSyncJob(runId + ":sync-" + sequence, maxAttempts, index == 2 ? 1 : 0);
            });
        }
        return jobs;
    }
}
