package com.caeliusconsulting.jobqueuesim;

/** Provides a logging contract plus reusable Java 8 default behavior. */
public interface Loggable {
    /** Writes a message through the implementing type's logging strategy. */
    void log(String message);

    /** Prints a visual separator shared by all logging implementations. */
    default void logSeparator() {
        System.out.println("---");
    }
}
