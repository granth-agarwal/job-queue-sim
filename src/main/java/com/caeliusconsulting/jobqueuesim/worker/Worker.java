package com.caeliusconsulting.jobqueuesim.worker;

import com.caeliusconsulting.jobqueuesim.domain.Job;
import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;
import com.caeliusconsulting.jobqueuesim.util.LogFormatter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

final class Worker implements Runnable {
    private final BlockingQueue<Job> queue;
    private final JobRepository repository;
    private final WorkerPool pool;

    Worker(BlockingQueue<Job> queue, JobRepository repository, WorkerPool pool) {
        this.queue = queue;
        this.repository = repository;
        this.pool = pool;
    }

    @Override
    public void run() {
        try {
            while (pool.isRunning()) {
                Job job = queue.poll(100, TimeUnit.MILLISECONDS);
                if (job != null) {
                    process(job);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pool.fail(new IllegalStateException("Worker interrupted", e));
        } catch (RuntimeException e) {
            pool.fail(e);
        } catch (Error e) {
            pool.fail(new IllegalStateException("Worker failed", e));
            throw e;
        }
    }

    private void process(Job job) throws InterruptedException {
        job.beginAttempt();
        if (!repository.claim(job)) {
            throw new DatabaseException("Cannot claim queued job " + job.getJobId());
        }
        LogFormatter.job("INFO", job, "PROCESSING attempt " + job.getAttemptCount()
                + "/" + job.getMaxAttempts());
        try {
            job.execute();
        } catch (JobExecutionException e) {
            if (job.canRetry(e)) {
                job.requeue(e.getMessage());
                repository.update(job);
                LogFormatter.job("WARN", job, "RETRY attempt " + job.getAttemptCount()
                        + "/" + job.getMaxAttempts() + ": " + e.getMessage());
                pool.requeue(job);
            } else {
                failJob(job, e.getMessage());
            }
            return;
        } catch (InterruptedException e) {
            pool.fail(new IllegalStateException("Execution interrupted", e));
            try {
                failJob(job, "Execution interrupted");
            } catch (RuntimeException persistenceFailure) {
                e.addSuppressed(persistenceFailure);
            } finally {
                Thread.currentThread().interrupt();
            }
            throw e;
        } catch (RuntimeException e) {
            pool.fail(e);
            try {
                failJob(job, "Unexpected execution failure: " + e.getMessage());
            } catch (RuntimeException persistenceFailure) {
                e.addSuppressed(persistenceFailure);
            }
            throw e;
        }
        job.complete();
        repository.update(job);
        LogFormatter.job("INFO", job, "COMPLETED");
        pool.jobFinished();
    }

    private void failJob(Job job, String message) {
        job.fail(message);
        repository.update(job);
        LogFormatter.job("WARN", job, "FAILED: " + message);
        pool.jobFinished();
    }
}
