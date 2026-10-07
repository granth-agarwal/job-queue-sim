package com.caeliusconsulting.jobqueuesim.collections;

import com.caeliusconsulting.jobqueuesim.jobs.DataSyncJob;
import com.caeliusconsulting.jobqueuesim.jobs.EmailJob;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.ReportGenerationJob;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Demonstrates two Queue use-cases.
 *
 * Queue: FIFO (First-In, First-Out). Core operations:
 *   offer() / add()   — enqueue
 *   poll() / remove() — dequeue (poll returns null; remove throws if empty)
 *   peek()            — view head without removing
 *
 * The actual concurrent queue in the pipeline uses LinkedBlockingQueue
 * (also shown here as Example 2).
 *
 * Syllabus: Queue interface, LinkedList as Queue, LinkedBlockingQueue,
 *           FIFO, thread-safe queues, BlockingQueue
 */
public class QueueDemo {

    public static void runDemo() {
        System.out.println("\n=== Queue Demo ===");
        demoFifoQueue();
        demoBlockingQueue();
    }

    /**
     * Example 1 — Simple FIFO Queue backed by LinkedList.
     *
     * Jobs enter the back and leave from the front — natural FIFO ordering.
     * Shows: offer(), peek(), poll(), isEmpty().
     */
    private static void demoFifoQueue() {
        System.out.println("\n[Queue-1] Simple FIFO queue (LinkedList as Queue):");

        Queue<Job> fifo = new LinkedList<>();
        fifo.offer(new EmailJob("q-email-1", 3));
        fifo.offer(new ReportGenerationJob("q-report-1", 3));
        fifo.offer(new DataSyncJob("q-sync-1", 3, 0));

        System.out.println("  Queue size: " + fifo.size());
        System.out.println("  Head (peek): " + fifo.peek());

        System.out.println("  Draining queue in FIFO order:");
        while (!fifo.isEmpty()) {
            Job job = fifo.poll();  // removes and returns head; null if empty
            System.out.println("    Dequeued: " + job);
        }
        System.out.println("  Queue empty: " + fifo.isEmpty());
    }

    /**
     * Example 2 — LinkedBlockingQueue (thread-safe, used in the real pipeline).
     *
     * LinkedBlockingQueue is the actual queue used by Workers in this POC.
     * put() blocks if the queue is full (unbounded here, so it never blocks).
     * take() blocks until an item is available — used by Worker.run().
     *
     * This demo exercises it in a single-threaded context to show the API.
     * In production, Workers call take() from separate threads.
     *
     * Shows: put(), take(), size().
     */
    private static void demoBlockingQueue() {
        System.out.println("\n[Queue-2] LinkedBlockingQueue (used in real worker pipeline):");

        LinkedBlockingQueue<Job> blockingQueue = new LinkedBlockingQueue<>();

        try {
            blockingQueue.put(new EmailJob("bq-email-1", 3));
            blockingQueue.put(new EmailJob("bq-email-2", 3));
            System.out.println("  Enqueued 2 jobs. Queue size: " + blockingQueue.size());

            // take() — removes head; blocks if empty (non-blocking here since items exist)
            Job first = blockingQueue.take();
            System.out.println("  take() returned: " + first);
            System.out.println("  Remaining queue size: " + blockingQueue.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("  BlockingQueue demo interrupted");
        }
    }
}
