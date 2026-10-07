package com.caeliusconsulting.jobqueuesim.util;

import com.caeliusconsulting.jobqueuesim.domain.ExecutionSummary;
import com.caeliusconsulting.jobqueuesim.domain.Job;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class LogFormatter {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private LogFormatter() { }

    public static void info(String message) {
        write("INFO", "-", message);
    }

    public static void job(String level, Job job, String event) {
        String id = job.getJobId();
        write(level, id.substring(id.lastIndexOf(':') + 1), event);
    }

    public static void error(String message) {
        write("ERROR", "-", message);
    }

    private static void write(String level, String jobId, String event) {
        System.out.println(String.format("%s  %-12s %-5s %-14s %s",
                LocalTime.now().format(TIME), "[" + Thread.currentThread().getName() + "]",
                level, jobId, event));
    }

    public static void summary(ExecutionSummary summary, int workerCount) {
        StringBuilder output = new StringBuilder("\nEXECUTION SUMMARY\n")
                .append("Submitted       : ").append(summary.submitted()).append('\n')
                .append("Completed       : ").append(summary.completed()).append('\n')
                .append("Failed          : ").append(summary.failed()).append('\n')
                .append("Retries         : ").append(summary.retries()).append('\n')
                .append("Total attempts  : ").append(summary.totalAttempts()).append('\n')
                .append("Worker threads  : ").append(workerCount).append('\n');
        System.out.println(output);
    }
}
