package com.caeliusconsulting.jobqueuesim.exceptions;

/**
 * Thrown when a Job is constructed with invalid or missing configuration.
 *
 * Unchecked (RuntimeException) because invalid configuration is a programming
 * error — it should be caught at dev time, not silently handled at runtime.
 *
 * Syllabus: Unchecked exceptions, RuntimeException
 */
public class InvalidJobConfigException extends RuntimeException {

    public InvalidJobConfigException(String message) {
        super(message);
    }
}
