package com.caeliusconsulting.jobqueuesim;

import java.util.List;

/** Formats single-threaded messages efficiently with StringBuilder. */
public final class LogFormatter {
    private LogFormatter() {
    }

    /** Builds one consistently formatted job-log line. */
    public static String formatJobLog(String jobId, String status, long timestamp) {
        return new StringBuilder()
                .append('[').append(timestamp).append("] JOB ")
                .append(jobId).append(" :: ").append(status)
                .toString();
    }

    /** Joins job identifiers into a concise reviewer-facing summary. */
    public static String buildSummary(List<String> jobIds) {
        StringBuilder summary = new StringBuilder("Jobs: ");
        for (int index = 0; index < jobIds.size(); index++) {
            if (index > 0) {
                summary.append(" | ");
            }
            summary.append(jobIds.get(index));
        }
        return summary.toString();
    }
}
