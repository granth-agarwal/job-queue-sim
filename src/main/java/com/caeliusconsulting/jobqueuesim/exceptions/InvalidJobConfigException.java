package com.caeliusconsulting.jobqueuesim.exceptions;

public final class InvalidJobConfigException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidJobConfigException(String message) {
        super(message);
    }
}
