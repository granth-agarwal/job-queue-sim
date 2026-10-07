package com.caeliusconsulting.jobqueuesim.worker;

import com.caeliusconsulting.jobqueuesim.config.WorkloadProfile;

public final class WorkerCountPolicy {
    private WorkerCountPolicy() { }

    public static int calculate(int availableProcessors, WorkloadProfile profile, int workerLimit, int jobCount, int queueCapacity) {
        if (availableProcessors < 1 || workerLimit < 1 || jobCount < 0 || queueCapacity < 1) {
            throw new IllegalArgumentException("Invalid worker sizing inputs");
        }
        long suggested = switch (profile) {
            case CPU_BOUND -> availableProcessors;
            case IO_BOUND -> 2L * availableProcessors;
            case MIXED -> availableProcessors + Math.max(1L, availableProcessors / 2L);
        };
        return (int) Math.min(suggested, Math.min(workerLimit, Math.min(jobCount, queueCapacity)));
    }
}
