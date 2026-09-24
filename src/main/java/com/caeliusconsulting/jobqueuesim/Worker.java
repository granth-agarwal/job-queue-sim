package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;

import java.time.LocalTime;

public class Worker {

    static int totalJobsProcessed = 0;
    static final StringBuilder sharedLog = new StringBuilder();

    public void processJob(Job job) {
        try {
            job.execute();
        } catch (JobExecutionException exception) {
            sharedLog.append(
                    LogFormatter.formatJobLog(
                            job.getJobId(),
                            "FAILURE: " + exception.getMessage(),
                            LocalTime.now()
                    ).concat(System.lineSeparator())
            );

            job.log("Handled failure: " + exception.getMessage());
        } finally {
            sharedLog.append(
                    LogFormatter.formatJobLog(
                            job.getJobId(),
                            "ATTEMPT COMPLETE",
                            LocalTime.now()
                    ).concat(System.lineSeparator())
            );

            totalJobsProcessed++;
        }
    }
}