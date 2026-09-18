package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.InvalidJobConfigException;
import com.caeliusconsulting.jobqueuesim.exceptions.RetryLimitExceededException;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        System.out.println("[Header] Job Queue Simulation");
        Loggable sectionLogger = new Loggable() {
            @Override
            public void log(String message) {
                System.out.println("[Loggable] " + message);
            }
        };
        sectionLogger.logSeparator();

        System.out.println("[Builder / this] Building three chained job configurations:");
        Job emailJob = new JobBuilder()
                .setId("email-1")
                .setType(JobType.EMAIL)
                .build();
        Job reportJob = new JobBuilder()
                .setId("report-1")
                .setType(JobType.REPORT)
                .build();
        Job dataSyncJob = new JobBuilder()
                .setId("sync-1")
                .setType(JobType.DATA_SYNC)
                .build();

        Worker worker = new Worker();
        System.out.println("[Polymorphism] Running EmailJob through a Job reference:");
        worker.processJob(emailJob);
        System.out.println("[Polymorphism] Running ReportGenerationJob through a Job reference:");
        worker.processJob(reportJob);
        System.out.println("[Polymorphism] Running DataSyncJob through a Job reference:");
        worker.processJob(dataSyncJob);

        System.out.println("[Checked Exception] Running a deliberately failing DataSyncJob:");
        worker.processJob(new DataSyncJob("sync-failure", true));

        System.out.println("[Inheritance + Interface + super] Exercising RetryableJob:");
        RetryableJob retryableJob = new RetryableJob("retry-1");
        worker.processJob(retryableJob);
        try {
            for (int attempt = 1; attempt <= RetryableJob.MAX_RETRY_ATTEMPTS + 1; attempt++) {
                retryableJob.retry();
                System.out.println("[Retry] Recorded retry " + retryableJob.getRetryCount());
                if (attempt <= 2) {
                    worker.processJob(retryableJob);
                }
            }
        } catch (RetryLimitExceededException exception) {
            System.out.println("[Checked Exception] Handled retry limit: " + exception.getMessage());
        }

        System.out.println("[Unchecked Exception] Building an invalid job configuration:");
        try {
            new JobBuilder().build();
        } catch (InvalidJobConfigException exception) {
            System.out.println("[Unchecked Exception] Handled invalid config: "
                    + exception.getMessage());
        }

        System.out.println("[StringBuffer + Threads] Processing two more jobs in parallel:");
        worker.runInParallel(new EmailJob("email-parallel"),
                new ReportGenerationJob("report-parallel"));

        System.out.println("[StringBuilder Summary] " + LogFormatter.buildSummary(
                List.of("email-1", "report-1", "sync-1", "retry-1")));
        System.out.println("[Static Counter] Total attempts processed: "
                + Worker.totalJobsProcessed);
        System.out.println("[Shared StringBuffer] Full worker log:\n" + Worker.sharedLog);
    }
}
