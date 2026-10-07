package com.caeliusconsulting.jobqueuesim.collections;

import com.caeliusconsulting.jobqueuesim.jobs.EmailJob;
import com.caeliusconsulting.jobqueuesim.jobs.Job;
import com.caeliusconsulting.jobqueuesim.jobs.ReportGenerationJob;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates two ArrayList use-cases.
 *
 * ArrayList: backed by a resizable array. O(1) random access, O(n) insertion
 * at arbitrary positions. Best when you need indexed access and infrequent
 * insertion/deletion in the middle.
 *
 * Syllabus: ArrayList, generics, iteration, List interface
 */
public class ArrayListDemo {

    public static void runDemo() {
        System.out.println("\n=== ArrayList Demo ===");
        demoBatchJobCollection();
        demoCompletedJobHistory();
    }

    /**
     * Example 1 — Job batch collection.
     *
     * An ArrayList holding a batch of pending jobs before they are dispatched.
     * Shows: add(), size(), get(), iteration with enhanced for-loop.
     */
    private static void demoBatchJobCollection() {
        System.out.println("\n[ArrayList-1] Batch job collection:");

        List<Job> batch = new ArrayList<>();
        batch.add(new EmailJob("email-batch-1"));
        batch.add(new EmailJob("email-batch-2"));
        batch.add(new ReportGenerationJob("report-batch-1"));

        System.out.println("  Batch size: " + batch.size());
        for (int i = 0; i < batch.size(); i++) {
            // O(1) indexed access — hallmark of ArrayList
            System.out.println("  [" + i + "] " + batch.get(i));
        }
    }

    /**
     * Example 2 — Completed job ID history.
     *
     * Appends job IDs as jobs complete and supports contains() lookup.
     * Shows: add(), contains(), remove(), size().
     */
    private static void demoCompletedJobHistory() {
        System.out.println("\n[ArrayList-2] Completed job ID history:");

        List<String> history = new ArrayList<>();
        history.add("email-1");
        history.add("report-1");
        history.add("sync-1");

        System.out.println("  History: " + history);
        System.out.println("  Contains 'report-1'? " + history.contains("report-1"));
        System.out.println("  Contains 'unknown-99'? " + history.contains("unknown-99"));

        history.remove("sync-1");
        System.out.println("  After removing 'sync-1': " + history);
        System.out.println("  Size: " + history.size());
    }
}
