package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.RetryLimitExceededException;

public interface Retryable {
    void retry() throws RetryLimitExceededException;

    int getRetryCount();
}
