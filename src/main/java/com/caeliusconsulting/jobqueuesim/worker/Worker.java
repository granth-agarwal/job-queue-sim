package com.caeliusconsulting.jobqueuesim.worker;

import com.caeliusconsulting.jobqueuesim.exceptions.DatabaseException;
import com.caeliusconsulting.jobqueuesim.exceptions.JobExecutionException;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public final class Worker implements Runnable {
    private final Queue<Job> queue;
    private final JobRepository repository;
    private final AtomicInteger remainingJobs;
    private final ExecutionHistory history;
    private RuntimeException failure;

    public Worker(Queue<Job> queue, JobRepository repository, AtomicInteger remainingJobs,
                  ExecutionHistory history) {
        this.queue = queue;
        this.repository = repository;
        this.remainingJobs = remainingJobs;
        this.history = history;
    }

    public RuntimeException getFailure() {
        return failure;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Worker started%n", Thread.currentThread().getName());
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Job job;
                synchronized (queue) {
                    while (queue.isEmpty() && remainingJobs.get() > 0) {
                        queue.wait();
                    }
                    if (remainingJobs.get() == 0) {
                        break;
                    }
                    job = queue.poll();
                }
                process(job);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failure = new IllegalStateException("Worker interrupted", e);
            stopWorkers();
        } catch (RuntimeException e) {
            failure = e;
            stopWorkers();
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
                synchronized (queue) {
                    queue.offer(job);
                    queue.notifyAll();
                }
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
        synchronized (queue) {
            if (remainingJobs.get() > 0 && remainingJobs.decrementAndGet() == 0) {
                queue.notifyAll();
            }
        }
    }

    private void stopWorkers() {
        synchronized (queue) {
            remainingJobs.set(0);
            queue.notifyAll();
        }
    }

    private void log(Job job, String event) {
        System.out.printf("[%s] %-12s %s%n", Thread.currentThread().getName(), job.getDisplayId(), event);
        history.record(Thread.currentThread().getName() + " " + job.getDisplayId() + " " + event);
    }
}
