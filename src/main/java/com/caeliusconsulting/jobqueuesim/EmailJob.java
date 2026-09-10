package com.caeliusconsulting.jobqueuesim;

/** Sends an email to demonstrate one concrete polymorphic job behavior. */
public class EmailJob extends Job {
    /** Creates an email job with the supplied identifier. */
    public EmailJob(String jobId) {
        super(jobId);
    }

    /** Simulates sending an email. */
    @Override
    public void execute() {
        log("Sending email");
    }
}
