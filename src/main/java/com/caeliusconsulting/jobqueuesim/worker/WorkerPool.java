package com.caeliusconsulting.jobqueuesim.worker;

import com.caeliusconsulting.jobqueuesim.domain.Job;
import com.caeliusconsulting.jobqueuesim.repository.JobRepository;
import com.caeliusconsulting.jobqueuesim.util.LogFormatter;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Lifecycle and submission methods are owned by a single producer. */
public final class WorkerPool implements AutoCloseable {
    private static final long POLL_MILLIS = 100;
    private static final long SHUTDOWN_SECONDS = 5;

    private final JobRepository repository;
    private final BlockingQueue<Job> queue;
    private final Semaphore admission;
    private final CountDownLatch completion;
    private final ExecutorService executor;
    private final int workerCount;
    private final int batchSize;
    private final Duration batchTimeout;
    private final AtomicReference<RuntimeException> failure = new AtomicReference<>();
    private volatile boolean accepting;
    private volatile boolean closing;
    private boolean started;
    private int submitted;
    private long deadline;

    public WorkerPool(JobRepository repository, int workerCount, int queueCapacity,
                      int batchSize, Duration batchTimeout) {
        if (workerCount < 1 || workerCount > queueCapacity || batchSize < workerCount
                || batchTimeout.isNegative() || batchTimeout.isZero()) {
            throw new IllegalArgumentException("Invalid worker pool configuration");
        }
        this.repository = repository;
        this.workerCount = workerCount;
        this.batchSize = batchSize;
        this.batchTimeout = batchTimeout;
        this.queue = new ArrayBlockingQueue<>(queueCapacity, true);
        // Bound unfinished jobs so a retry never blocks workers behind a full queue.
        this.admission = new Semaphore(queueCapacity);
        this.completion = new CountDownLatch(batchSize);
        AtomicInteger threadSequence = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(workerCount, task ->
                new Thread(task, "worker-" + threadSequence.incrementAndGet()));
    }

    public void start() {
        if (started || executor.isShutdown()) {
            throw new IllegalStateException("Worker pool cannot be started again");
        }
        started = true;
        accepting = true;
        deadline = System.nanoTime() + batchTimeout.toNanos();
        for (int index = 0; index < workerCount; index++) {
            executor.execute(new Worker(queue, repository, this));
        }
        LogFormatter.info("Worker pool started");
    }

    public void submit(Job job) throws InterruptedException {
        checkAccepting();
        while (!admission.tryAcquire(POLL_MILLIS, TimeUnit.MILLISECONDS)) {
            checkAccepting();
        }
        boolean enqueued = false;
        try {
            checkAccepting();
            if (submitted >= batchSize) {
                throw new IllegalStateException("Batch submission limit reached");
            }
            repository.create(job);
            LogFormatter.job("INFO", job, "QUEUED [" + job.getType() + "]");
            queue.add(job);
            submitted++;
            enqueued = true;
        } finally {
            if (!enqueued) {
                admission.release();
            }
        }
    }

    public void finishSubmission() {
        if (submitted != batchSize) {
            throw new IllegalStateException("Expected " + batchSize + " submitted jobs, received " + submitted);
        }
        accepting = false;
    }

    public void awaitCompletion() throws InterruptedException {
        if (!started) {
            throw new IllegalStateException("Worker pool has not started");
        }
        while (!completion.await(POLL_MILLIS, TimeUnit.MILLISECONDS)) {
            checkHealthy();
        }
        checkHealthy();
    }

    boolean isRunning() {
        return failure.get() == null && (accepting || !queue.isEmpty() || (!closing && completion.getCount() > 0));
    }

    void requeue(Job job) {
        LogFormatter.job("INFO", job, "RE-QUEUED");
        queue.add(job);
    }

    void jobFinished() {
        completion.countDown();
        admission.release();
    }

    void fail(RuntimeException cause) {
        failure.compareAndSet(null, cause);
        accepting = false;
    }

    private void checkAccepting() {
        if (!started || executor.isShutdown()) {
            throw new IllegalStateException("Worker pool is not accepting jobs");
        }
        checkHealthy();
        if (!accepting) {
            throw new IllegalStateException("Worker pool is not accepting jobs");
        }
    }

    private void checkHealthy() {
        RuntimeException cause = failure.get();
        if (cause != null) {
            throw new IllegalStateException("Worker pool aborted", cause);
        }
        if (System.nanoTime() - deadline >= 0) {
            throw new IllegalStateException("Batch exceeded " + batchTimeout.toSeconds() + " seconds");
        }
    }

    @Override
    public void close() {
        accepting = false;
        closing = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_SECONDS, TimeUnit.SECONDS)) {
                LogFormatter.error("Worker shutdown timed out; interrupting remaining work");
                executor.shutdownNow();
                if (!executor.awaitTermination(SHUTDOWN_SECONDS, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Worker threads did not terminate");
                }
                throw new IllegalStateException("Worker shutdown required interruption");
            }
            LogFormatter.info("Worker pool shut down successfully");
        } catch (InterruptedException e) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(SHUTDOWN_SECONDS, TimeUnit.SECONDS)) {
                    e.addSuppressed(new IllegalStateException("Worker threads did not terminate"));
                }
            } catch (InterruptedException repeated) {
                e.addSuppressed(repeated);
            } finally {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Interrupted while shutting down workers", e);
        }
    }
}
