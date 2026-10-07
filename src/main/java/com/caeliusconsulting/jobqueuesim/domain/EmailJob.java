package com.caeliusconsulting.jobqueuesim.domain;

public final class EmailJob extends Job {
    public EmailJob(String jobId, int maxAttempts) {
        super(jobId, maxAttempts);
    }

    @Override
    public JobType getType() {
        return JobType.EMAIL;
    }

    @Override
    public void execute() throws InterruptedException {
        Thread.sleep(150);
    }
}
