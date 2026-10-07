package com.caeliusconsulting.jobqueuesim.jobs;

/**
 * Sends an email notification.
 *
 * Simulates work with a short sleep so concurrency is observable.
 *
 * Syllabus: Inheritance, polymorphism, method overriding
 */
public class EmailJob extends Job {

    public EmailJob(String jobId) {
        super(jobId);
    }

    @Override
    public JobType getType() {
        return JobType.EMAIL;
    }

    @Override
    public void execute() {
        logStart();
        String recipient = getJobId() + "@example.test";
        log("Preparing email to " + recipient);
        simulateWork(150);
        log("Email delivered to " + recipient);
    }

    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
