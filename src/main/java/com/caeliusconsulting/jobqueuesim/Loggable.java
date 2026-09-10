package com.caeliusconsulting.jobqueuesim;

public interface Loggable {
    void log(String message);

    default void logSeparator() {
        System.out.println("---");
    }
}
