package com.caeliusconsulting.jobqueuesim;

public class ReportGenerationJob extends Job {
    public ReportGenerationJob(String jobId) {
        super(jobId);
    }

    @Override
    public void execute() {
        int sectionCount = Math.max(1, getJobId().length() / 2);
        log("Generating report with " + sectionCount + " sections");
    }
}
