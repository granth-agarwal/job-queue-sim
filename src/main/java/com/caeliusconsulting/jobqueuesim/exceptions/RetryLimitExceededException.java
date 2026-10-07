package com.caeliusconsulting.jobqueuesim.exceptions;

/**
 * Thrown when retry attempts for a job exceed the configured maximum.
 *
 * Checked because exceeding the limit is an expected, recoverable outcome
 * that callers should explicitly handle.
 *
 * Syllabus: Checked exceptions
 */
public class RetryLimitExceededException extends Exception {

    public RetryLimitExceededException(String message) {
        super(message);
    }
}
