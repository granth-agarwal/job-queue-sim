package com.caeliusconsulting.jobqueuesim.exceptions;

public class RetryLimitExceededException extends Exception {
    private static final long serialVersionUID = 1L;

    public RetryLimitExceededException(String message) {
        super(message);
    }
}
