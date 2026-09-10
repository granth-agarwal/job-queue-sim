package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.RetryLimitExceededException;

/** Defines an optional capability contract because not every job needs retries. */
public interface Retryable {
    /** Records a retry while enforcing the implementation's retry limit. */
    void retry() throws RetryLimitExceededException;

    int getRetryCount();
}
