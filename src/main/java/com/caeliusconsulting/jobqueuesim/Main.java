package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.config.AppConfig;
import com.caeliusconsulting.jobqueuesim.config.DataSourceFactory;
import com.caeliusconsulting.jobqueuesim.domain.Job;
import com.caeliusconsulting.jobqueuesim.repository.JdbcJobRepository;
import com.caeliusconsulting.jobqueuesim.util.LogFormatter;
import com.caeliusconsulting.jobqueuesim.worker.WorkerCountPolicy;
import com.caeliusconsulting.jobqueuesim.worker.WorkerPool;

import java.util.List;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        LogFormatter.info("Job Queue Simulator starting");
        try {
            run(AppConfig.fromEnvironment());
            LogFormatter.info("Job Queue Simulator finished");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LogFormatter.error("Application interrupted");
            System.exit(1);
        } catch (RuntimeException e) {
            LogFormatter.error("Application failed: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void run(AppConfig config) throws InterruptedException {
        try (var dataSource = DataSourceFactory.create(config)) {
            JdbcJobRepository repository = new JdbcJobRepository(dataSource);
            repository.initSchema();
            LogFormatter.info("Database initialized");
            List<Job> jobs = DemoBatch.create(config.batchSize(), config.maxJobAttempts());
            int processors = Runtime.getRuntime().availableProcessors();
            int workerCount = WorkerCountPolicy.calculate(processors, config.workloadProfile(),
                    config.workerLimit(), jobs.size(), config.queueCapacity());
            LogFormatter.info("Available processors : " + processors);
            LogFormatter.info("Workload profile     : " + config.workloadProfile());
            LogFormatter.info("Worker threads       : " + workerCount);
            LogFormatter.info("Queue capacity       : " + config.queueCapacity());
            LogFormatter.info("Database pool limit  : " + config.dbPoolSize());
            try (WorkerPool workers = new WorkerPool(repository, workerCount, config.queueCapacity(),
                    jobs.size(), config.batchTimeout())) {
                workers.start();
                for (Job job : jobs) {
                    workers.submit(job);
                }
                workers.finishSubmission();
                LogFormatter.info(jobs.size() + " jobs submitted successfully");
                workers.awaitCompletion();
                LogFormatter.summary(repository.summarize(jobs.stream().map(Job::getJobId).toList()), workerCount);
            }
        }
    }
}
