package com.caeliusconsulting.jobqueuesim.exceptions;

// Unchecked because invalid configuration is preventable, unlike recoverable execution failures.
public class InvalidJobConfigException extends RuntimeException {
    public InvalidJobConfigException(String message) {
        super(message);
    }
}
