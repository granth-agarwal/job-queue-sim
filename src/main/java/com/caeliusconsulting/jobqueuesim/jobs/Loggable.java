package com.caeliusconsulting.jobqueuesim.jobs;

/**
 * Simple logging interface implemented by Job and used as a functional interface
 * for anonymous class demonstration in Main (via the legacy learnings path).
 *
 * Syllabus: Interfaces, default methods
 */
public interface Loggable {

    void log(String message);

    /** Default method — no override required. */
    default void logSeparator() {
        System.out.println("---");
    }
}
