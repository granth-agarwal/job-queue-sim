package com.caeliusconsulting.jobqueuesim.exceptions;

/** Thrown when a retryable job exceeds its maximum permitted attempts. */
public class RetryLimitExceededException extends Exception {
    private static final long serialVersionUID = 1L;

    /** Creates a checked retry-limit failure. */
    public RetryLimitExceededException(String message) {
        super(message);
    }
}
