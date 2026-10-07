package com.caeliusconsulting.jobqueuesim.exceptions;

public final class JobExecutionException extends Exception {
    private static final long serialVersionUID = 1L;
    private final boolean transientFailure;

    public JobExecutionException(String message, boolean transientFailure) {
        super(message);
        this.transientFailure = transientFailure;
    }

    public boolean isTransient() {
        return transientFailure;
    }
}
