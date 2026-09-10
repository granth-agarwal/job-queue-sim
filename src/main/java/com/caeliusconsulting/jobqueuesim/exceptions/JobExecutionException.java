package com.caeliusconsulting.jobqueuesim.exceptions;

/** Thrown when a job's own execution logic fails. */
public class JobExecutionException extends Exception {
    private static final long serialVersionUID = 1L;

    /** Creates a checked job failure with a clean user-facing message. */
    public JobExecutionException(String message) {
        super(message);
    }
}
