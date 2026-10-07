package com.caeliusconsulting.jobqueuesim.collections;

import com.caeliusconsulting.jobqueuesim.jobs.DataSyncJob;
import com.caeliusconsulting.jobqueuesim.jobs.EmailJob;
import com.caeliusconsulting.jobqueuesim.jobs.Job;

import java.util.LinkedList;

/**
 * Demonstrates two LinkedList use-cases.
 *
 * LinkedList: doubly-linked node chain. O(1) insertion/removal at head/tail,
 * O(n) random access. Best when you need frequent insertion/removal at both ends.
 * Implements both List and Deque interfaces.
 *
 * Syllabus: LinkedList, Deque behaviour, addFirst/addLast/removeFirst/peekFirst
 */
public class LinkedListDemo {

    public static void runDemo() {
        System.out.println("\n=== LinkedList Demo ===");
        demoProcessingHistory();
        demoAuditLog();
    }

    /**
     * Example 1 — Ordered processing history.
     *
     * Uses LinkedList as a double-ended queue (Deque) to maintain a rolling
     * history of recently processed jobs.
     * Shows: addLast(), addFirst(), removeFirst(), peekFirst(), size().
     */
    private static void demoProcessingHistory() {
        System.out.println("\n[LinkedList-1] Processing history (Deque behaviour):");

        LinkedList<Job> history = new LinkedList<>();

        // addLast() — append to tail (O(1))
        history.addLast(new EmailJob("email-ll-1", 3));
        history.addLast(new DataSyncJob("sync-ll-1", 3, 0));

        // addFirst() — prepend to head (O(1)) — most recent at front
        history.addFirst(new EmailJob("email-ll-urgent", 3));

        System.out.println("  Head (most recent): " + history.peekFirst());
        System.out.println("  Tail (oldest):      " + history.peekLast());
        System.out.println("  History size: " + history.size());

        // removeFirst() — O(1) removal from head
        Job removed = history.removeFirst();
        System.out.println("  Removed from head: " + removed);
        System.out.println("  Remaining: " + history.size() + " items");
    }

    /**
     * Example 2 — Audit log.
     *
     * A LinkedList<String> acting as an append-only audit trail.
     * Shows: add(), peek(), remove(), iteration.
     */
    private static void demoAuditLog() {
        System.out.println("\n[LinkedList-2] Audit log:");

        LinkedList<String> auditLog = new LinkedList<>();
        auditLog.add("Job email-1 QUEUED");
        auditLog.add("Job email-1 PROCESSING");
        auditLog.add("Job email-1 COMPLETED");
        auditLog.add("Job sync-1 QUEUED");
        auditLog.add("Job sync-1 FAILED");

        System.out.println("  Latest entry: " + auditLog.peekLast());
        System.out.println("  Oldest entry: " + auditLog.peekFirst());
        System.out.println("  Total audit entries: " + auditLog.size());

        // Drain and print all entries (destructive iteration)
        System.out.println("  Full audit log:");
        while (!auditLog.isEmpty()) {
            System.out.println("    → " + auditLog.removeFirst());
        }
    }
}
