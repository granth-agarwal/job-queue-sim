package com.caeliusconsulting.jobqueuesim.config;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

public record AppConfig(String dbUrl, String dbUser, String dbPassword, int queueCapacity,
                        int maxJobAttempts, WorkloadProfile workloadProfile, int workerLimit,
                        int dbPoolSize, int batchSize, Duration batchTimeout) {
    public static AppConfig fromEnvironment() {
        return from(System.getenv());
    }

    public static AppConfig from(Map<String, String> environment) {
        String password = environment.getOrDefault("DB_PASSWORD", "");
        WorkloadProfile profile;
        try {
            profile = WorkloadProfile.valueOf(environment.getOrDefault("WORKLOAD_PROFILE", "IO_BOUND")
                    .trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("WORKLOAD_PROFILE must be CPU_BOUND, IO_BOUND, or MIXED", e);
        }
        return new AppConfig(required(environment, "DB_URL"), required(environment, "DB_USER"), password,
                positive(environment, "QUEUE_CAPACITY", 64), positive(environment, "MAX_JOB_ATTEMPTS", 3),
                profile, positive(environment, "WORKER_LIMIT", 16), positive(environment, "DB_POOL_SIZE", 4),
                positive(environment, "BATCH_SIZE", 9),
                Duration.ofSeconds(positive(environment, "BATCH_TIMEOUT_SECONDS", 120)));
    }

    @Override
    public String toString() {
        return "AppConfig[workloadProfile=" + workloadProfile + ", workerLimit=" + workerLimit
                + ", queueCapacity=" + queueCapacity + ", dbPoolSize=" + dbPoolSize + "]";
    }

    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required environment variable: " + name);
        }
        return value;
    }

    private static int positive(Map<String, String> environment, String name, int defaultValue) {
        try {
            int value = Integer.parseInt(environment.getOrDefault(name, Integer.toString(defaultValue)));
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a positive integer", e);
        }
        throw new IllegalArgumentException(name + " must be a positive integer");
    }
}
