package com.caeliusconsulting.jobqueuesim;

import java.time.LocalTime;
import java.util.List;

public final class LogFormatter {
    private LogFormatter() {
    }

    public static String formatJobLog(String jobId, String status, LocalTime timestamp) {
        return new StringBuilder()
                .append('[').append(timestamp).append("] JOB ")
                .append(jobId).append(" :: ").append(status)
                .toString();
    }

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
