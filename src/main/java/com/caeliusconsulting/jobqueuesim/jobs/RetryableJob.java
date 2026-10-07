package com.caeliusconsulting.jobqueuesim.jobs;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;
import com.caeliusconsulting.jobqueuesim.exceptions.RetryLimitExceededException;

/**
 * A job that deliberately fails until it has been retried enough times.
 *
 * NOTE: This class is a standalone OOP demo and is NOT wired into the
 * concurrent worker pipeline. The spec forbids retry logic in the pipeline.
 *
 * Demonstrates: multiple interface implementation, super keyword, checked exceptions.
 *
 * Syllabus: Inheritance, interfaces, super, checked exceptions
 */
public class RetryableJob extends Job implements Retryable {

    public static final int MAX_RETRY_ATTEMPTS = 3;

    private int retryCount = 0;

    public RetryableJob(String jobId) {
        super(jobId);
    }

    @Override
    public JobType getType() {
        // RetryableJob is a demo — it doesn't map to a real dispatch queue.
        return JobType.UNKNOWN;
    }

    @Override
    public void execute() throws JobExecutionException {
        super.logStart(); // super explicitly invokes behaviour from the abstract base class
        if (retryCount < 2) {
            throw new JobExecutionException("Retryable work is not ready yet");
        }
        log("Retryable work succeeded after " + retryCount + " retries");
    }

    @Override
    public void retry() throws RetryLimitExceededException {
        retryCount++;
        if (retryCount > MAX_RETRY_ATTEMPTS) {
            throw new RetryLimitExceededException("Maximum retry attempts exceeded");
        }
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }
}
