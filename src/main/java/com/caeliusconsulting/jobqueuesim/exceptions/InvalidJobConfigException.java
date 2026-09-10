package com.caeliusconsulting.jobqueuesim.exceptions;

/**
 * Unchecked because invalid configuration is a preventable programming error,
 * unlike the recoverable checked execution and retry failures.
 */
public class InvalidJobConfigException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** Creates an unchecked configuration failure. */
    public InvalidJobConfigException(String message) {
        super(message);
    }
}
