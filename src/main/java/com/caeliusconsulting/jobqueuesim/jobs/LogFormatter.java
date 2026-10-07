package com.caeliusconsulting.jobqueuesim.jobs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Stateless utility for formatting log messages.
 *
 * Uses StringBuilder internally to demonstrate its role in string building.
 * NOT shared across threads — each call creates a new instance.
 *
 * Syllabus: StringBuilder (thread-local usage)
 */
public final class LogFormatter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private LogFormatter() { }

    /** Format a single job log line. */
    public static String formatJobLog(String jobId, String status, LocalDateTime timestamp) {
        return new StringBuilder()
                .append('[').append(timestamp.format(FORMATTER)).append("] [")
                .append(Thread.currentThread().getName()).append("] JOB ")
                .append(jobId).append(" :: ").append(status)
                .toString();
    }

    /** Build a summary string from a list of job IDs. */
    public static String buildSummary(List<String> jobIds) {
        StringBuilder summary = new StringBuilder("Processed Jobs: ");
        for (int i = 0; i < jobIds.size(); i++) {
            if (i > 0) {
                summary.append(" | ");
            }
            summary.append(jobIds.get(i));
        }
        return summary.toString();
    }
}
