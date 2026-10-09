package com.caeliusconsulting.jobqueuesim;

import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.jobs.DataSyncJob;
import com.caeliusconsulting.jobqueuesim.jobs.EmailJob;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.ReportGenerationJob;
import com.caeliusconsulting.jobqueuesim.repository.JdbcJobRepository;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;
import com.caeliusconsulting.jobqueuesim.worker.ExecutionHistory;
import com.caeliusconsulting.jobqueuesim.worker.Worker;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class Main {
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

        Queue<Job> queue = new LinkedList<>();
        AtomicInteger remainingJobs = new AtomicInteger(jobs.size());
        ExecutionHistory history = new ExecutionHistory();
        int workerCount = Math.min(Runtime.getRuntime().availableProcessors(), jobs.size());
        List<Worker> workers = new LinkedList<>();
        List<Thread> threads = new LinkedList<>();
        try {
            for (int number = 1; number <= workerCount; number++) {
                Worker worker = new Worker(queue, repository, remainingJobs, history);
                Thread thread = new Thread(worker, "worker-" + number);
                workers.add(worker);
                threads.add(thread);
                thread.start();
            }
            System.out.printf("[main] %d worker threads started%n", workerCount);
            for (Job job : jobs) {
                repository.create(job);
                System.out.printf("[main] %-12s QUEUED [%s]%n", job.getDisplayId(), job.getType());
                synchronized (queue) {
                    queue.offer(job);
                    queue.notifyAll();
                }
            }
            for (Thread thread : threads) {
                thread.join();
            }
            for (Worker worker : workers) {
                if (worker.getFailure() != null) {
                    throw new IllegalStateException("Job processing stopped", worker.getFailure());
                }
            }
            printSummary(repository, jobs, workerCount, history);
        } finally {
            synchronized (queue) {
                remainingJobs.set(0);
                queue.notifyAll();
            }
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

    private static void printSummary(JobRepository repository, List<Job> jobs,
                                     int workerCount, ExecutionHistory history) {
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
                        + "Failed         : %d%nRetries        : %d%nTotal attempts : %d%nWorker threads : %d%n",
                jobs.size(), completed, failed, attempts - jobs.size(), attempts, workerCount);
        System.out.println("Execution history:");
        for (String event : history.snapshot()) {
            System.out.println("  " + event);
        }
        System.out.println();
    }
}
