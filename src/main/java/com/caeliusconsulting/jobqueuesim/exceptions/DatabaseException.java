package com.caeliusconsulting.jobqueuesim.exceptions;

/**
 * Wraps JDBC SQLExceptions to decouple the repository layer from
 * java.sql — callers do not need to import java.sql to handle DB errors.
 *
 * Unchecked because database failures are not preventable by the caller
 * and should propagate up to the top-level error handler.
 *
 * Always preserves the original cause (original SQLException) for
 * full stack-trace visibility.
 *
 * Syllabus: Exception wrapping, unchecked exceptions, cause chaining
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}
