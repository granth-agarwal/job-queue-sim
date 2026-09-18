package com.caeliusconsulting.jobqueuesim.exceptions;

public class RetryLimitExceededException extends Exception {
    public RetryLimitExceededException(String message) {
        super(message);
    }
}
