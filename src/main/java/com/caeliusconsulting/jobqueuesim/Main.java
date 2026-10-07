package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.jobs.DataSyncJob;
import com.caeliusconsulting.jobqueuesim.jobs.EmailJob;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.ReportGenerationJob;
import com.caeliusconsulting.jobqueuesim.repository.JdbcJobRepository;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;
import com.caeliusconsulting.jobqueuesim.worker.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class Main {
    private static final int WORKER_COUNT = 3;
    private static final int MAX_ATTEMPTS = 3;

    private Main() { }

    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[main] Application interrupted");
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.printf("[main] Application failed: %s%n", e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void run() throws InterruptedException {
        System.out.println("[main] Job Queue Simulator starting");
        JdbcJobRepository repository = new JdbcJobRepository();
        repository.initSchema();
        System.out.println("[main] Database ready");

        String runId = UUID.randomUUID() + ":";
        List<Job> jobs = List.of(
                new EmailJob(runId + "email-001", MAX_ATTEMPTS),
                new ReportGenerationJob(runId + "report-002", MAX_ATTEMPTS),
                new DataSyncJob(runId + "sync-003", MAX_ATTEMPTS, 1),
                new EmailJob(runId + "email-004", MAX_ATTEMPTS),
                new ReportGenerationJob(runId + "report-005", MAX_ATTEMPTS),
                new DataSyncJob(runId + "sync-006", MAX_ATTEMPTS, 0),
                new EmailJob(runId + "email-007", MAX_ATTEMPTS),
                new ReportGenerationJob(runId + "report-008", MAX_ATTEMPTS),
                new DataSyncJob(runId + "sync-009", MAX_ATTEMPTS, 0));

        BlockingQueue<Job> queue = new ArrayBlockingQueue<>(jobs.size(), true);
        AtomicInteger remainingJobs = new AtomicInteger(jobs.size());
        List<Worker> workers = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        try {
            for (int number = 1; number <= WORKER_COUNT; number++) {
                Worker worker = new Worker(queue, repository, remainingJobs);
                Thread thread = new Thread(worker, "worker-" + number);
                workers.add(worker);
                threads.add(thread);
                thread.start();
            }
            System.out.printf("[main] %d worker threads started%n", WORKER_COUNT);
            for (Job job : jobs) {
                repository.create(job);
                System.out.printf("[main] %-12s QUEUED [%s]%n", job.getDisplayId(), job.getType());
                queue.put(job);
            }
            for (Thread thread : threads) {
                thread.join();
            }
            for (Worker worker : workers) {
                if (worker.getFailure() != null) {
                    throw new IllegalStateException("Job processing stopped", worker.getFailure());
                }
            }
            printSummary(repository, jobs);
        } finally {
            remainingJobs.set(0);
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    thread.interrupt();
                }
            }
            for (Thread thread : threads) {
                thread.join();
            }
        }
        System.out.println("[main] Worker threads stopped");
        System.out.println("[main] Job Queue Simulator finished");
    }

    private static void printSummary(JobRepository repository, List<Job> jobs) {
        int completed = 0;
        int failed = 0;
        int attempts = 0;
        for (Job job : jobs) {
            switch (repository.findStatus(job.getJobId())) {
                case COMPLETED -> completed++;
                case FAILED -> failed++;
                default -> throw new DatabaseException("Job is unfinished: " + job.getJobId());
            }
            attempts += job.getAttemptCount();
        }
        System.out.printf("%nEXECUTION SUMMARY%nSubmitted      : %d%nCompleted      : %d%n"
                        + "Failed         : %d%nRetries        : %d%nTotal attempts : %d%nWorker threads : %d%n%n",
                jobs.size(), completed, failed, attempts - jobs.size(), attempts, WORKER_COUNT);
    }
}
