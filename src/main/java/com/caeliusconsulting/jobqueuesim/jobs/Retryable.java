package com.caeliusconsulting.jobqueuesim.jobs;

import com.caeliusconsulting.jobqueuesim.exceptions.RetryLimitExceededException;

/**
 * Interface for jobs that support retry logic.
 *
 * NOTE: Retry is NOT part of the concurrent worker pipeline.
 * This interface is retained as a standalone OOP/interface demo.
 *
 * The modern recommendation is to use the Deque/ArrayDeque-based approach
 * or a dedicated retry scheduler, not this polling pattern.
 *
 * Syllabus: Interfaces, checked exceptions
 */
public interface Retryable {
    void retry() throws RetryLimitExceededException;
    int getRetryCount();
}
