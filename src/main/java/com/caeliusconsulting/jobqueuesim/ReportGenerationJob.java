package com.caeliusconsulting.jobqueuesim;

/** Generates a report to demonstrate a second polymorphic job behavior. */
public class ReportGenerationJob extends Job {
    /** Creates a report job with the supplied identifier. */
    public ReportGenerationJob(String jobId) {
        super(jobId);
    }

    /** Simulates generating a report. */
    @Override
    public void execute() {
        log("Generating report");
    }
}
