package com.caeliusconsulting.jobqueuesim.worker;

import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class Worker implements Runnable {
    private final BlockingQueue<Job> queue;
    private final JobRepository repository;
    private final AtomicInteger remainingJobs;
    private RuntimeException failure;

    public Worker(BlockingQueue<Job> queue, JobRepository repository, AtomicInteger remainingJobs) {
        this.queue = queue;
        this.repository = repository;
        this.remainingJobs = remainingJobs;
    }

    public RuntimeException getFailure() {
        return failure;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Worker started%n", Thread.currentThread().getName());
        try {
            while (remainingJobs.get() > 0 && !Thread.currentThread().isInterrupted()) {
                Job job = queue.poll(100, TimeUnit.MILLISECONDS);
                if (job != null) {
                    process(job);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failure = new IllegalStateException("Worker interrupted", e);
            remainingJobs.set(0);
        } catch (RuntimeException e) {
            failure = e;
            remainingJobs.set(0);
        } finally {
            System.out.printf("[%s] Worker stopped%n", Thread.currentThread().getName());
        }
    }

    private void process(Job job) throws InterruptedException {
        job.beginAttempt();
        if (!repository.claim(job)) {
            throw new DatabaseException("Cannot claim queued job " + job.getJobId());
        }
        log(job, "PROCESSING attempt " + job.getAttemptCount() + "/" + job.getMaxAttempts());
        try {
            job.execute();
            job.complete();
        } catch (JobExecutionException e) {
            if (job.canRetry(e)) {
                job.requeue(e.getMessage());
                repository.update(job);
                log(job, "RETRY: " + e.getMessage());
                queue.put(job);
                return;
            }
            job.fail(e.getMessage());
        } catch (InterruptedException e) {
            job.fail("Execution interrupted");
            repository.update(job);
            throw e;
        }
        repository.update(job);
        log(job, job.getStatus().name());
        remainingJobs.decrementAndGet();
    }

    private static void log(Job job, String event) {
        System.out.printf("[%s] %-12s %s%n", Thread.currentThread().getName(), job.getDisplayId(), event);
    }
}
