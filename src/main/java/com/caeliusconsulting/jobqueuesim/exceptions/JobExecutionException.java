package com.caeliusconsulting.jobqueuesim.exceptions;

/**
 * Thrown when a job fails during execution.
 *
 * Checked because execution failures are recoverable — the worker catches this,
 * marks the job FAILED, and continues processing other jobs.
 *
 * Syllabus: Checked exceptions, exception hierarchy
 */
public class JobExecutionException extends Exception {

    public JobExecutionException(String message) {
        super(message);
    }

    public JobExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
