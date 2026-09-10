package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;
import com.caeliusconsulting.jobqueuesim.exceptions.RetryLimitExceededException;

public class RetryableJob extends Job implements Retryable {
    static final int MAX_RETRY_ATTEMPTS = 3;
    private int retryCount = 0;

    public RetryableJob(String jobId) {
        super(jobId);
    }

    @Override
    public void execute() throws JobExecutionException {
        super.logStart(); // super explicitly invokes behavior from the abstract base class.
        if (retryCount < 2) {
            throw new JobExecutionException("Retryable work is not ready yet");
        }
        log("Retryable work succeeded");
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
