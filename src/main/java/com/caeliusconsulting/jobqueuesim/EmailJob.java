package com.caeliusconsulting.jobqueuesim;

public class EmailJob extends Job {
    public EmailJob(String jobId) {
        super(jobId);
    }

    @Override
    public void execute() {
        String recipient = getJobId() + "@example.test";
        log("Sending email to " + recipient);
    }
}
