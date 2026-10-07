package com.caeliusconsulting.jobqueuesim.jobs;

public final class ReportGenerationJob extends Job {
    public ReportGenerationJob(String jobId, int maxAttempts) {
        super(jobId, maxAttempts);
    }

    @Override
    public JobType getType() {
        return JobType.REPORT;
    }

    @Override
    public void execute() throws InterruptedException {
        Thread.sleep(200);
    }
}
