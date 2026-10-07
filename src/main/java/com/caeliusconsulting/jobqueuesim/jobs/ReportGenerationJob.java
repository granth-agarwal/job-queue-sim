package com.caeliusconsulting.jobqueuesim.jobs;

/**
 * Generates a report document.
 *
 * Syllabus: Inheritance, polymorphism, method overriding
 */
public class ReportGenerationJob extends Job {

    public ReportGenerationJob(String jobId) {
        super(jobId);
    }

    @Override
    public JobType getType() {
        return JobType.REPORT;
    }

    @Override
    public void execute() {
        logStart();
        int sections = Math.max(1, getJobId().length() / 2);
        log("Generating report with " + sections + " sections");
        simulateWork(200);
        log("Report generation complete (" + sections + " sections written)");
    }

    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
