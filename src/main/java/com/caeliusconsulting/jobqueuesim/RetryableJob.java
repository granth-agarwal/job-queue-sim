package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;
import com.caeliusconsulting.jobqueuesim.exceptions.RetryLimitExceededException;

/** Combines abstract-class inheritance with the optional retry capability. */
public class RetryableJob extends Job implements Retryable {
    static final int MAX_RETRY_ATTEMPTS = 3;
    private int retryCount = 0;

    /** Creates a retryable job with no attempts used. */
    public RetryableJob(String jobId) {
        super(jobId);
    }

    /** Fails until two retries have occurred, then succeeds. */
    @Override
    public void execute() throws JobExecutionException {
        super.logStart(); // super explicitly invokes behavior from the abstract base class.
        if (retryCount < 2) {
            throw new JobExecutionException("Retryable work is not ready yet");
        }
        log("Retryable work succeeded");
    }

    /** Records an attempt and rejects attempts beyond the class-wide ceiling. */
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
