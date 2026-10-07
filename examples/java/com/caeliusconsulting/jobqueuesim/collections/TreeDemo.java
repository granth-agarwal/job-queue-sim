package com.caeliusconsulting.jobqueuesim.collections;

import com.caeliusconsulting.jobqueuesim.domain.JobType;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Demonstrates two tree-based collection use-cases.
 *
 * TreeSet  — sorted Set backed by a red-black tree. O(log n) add/remove/contains.
 *            Iteration always yields elements in natural (or comparator) order.
 *
 * TreeMap  — sorted Map backed by a red-black tree. O(log n) get/put/remove.
 *            Keys are always iterated in natural (or comparator) order.
 *
 * No custom tree is implemented — the spec requires use of Java's tree-based
 * collections, not a hand-rolled BST.
 *
 * Syllabus: TreeSet, TreeMap, sorted collections, NavigableSet/Map
 */
public class TreeDemo {

    public static void runDemo() {
        System.out.println("\n=== Tree Demo (TreeSet & TreeMap) ===");
        demoSortedJobIds();
        demoGroupedByType();
    }

    /**
     * Example 1 — TreeSet of sorted job IDs.
     *
     * Inserts job IDs in arbitrary order; TreeSet stores them in natural
     * alphabetical order automatically.
     * Shows: add(), first(), last(), headSet(), iteration in sorted order.
     */
    private static void demoSortedJobIds() {
        System.out.println("\n[TreeSet-1] Sorted job IDs:");

        TreeSet<String> jobIds = new TreeSet<>();
        // Deliberately inserted out of alphabetical order
        jobIds.add("sync-3");
        jobIds.add("email-1");
        jobIds.add("report-2");
        jobIds.add("email-2");
        jobIds.add("sync-1");

        System.out.println("  Sorted IDs: " + jobIds);
        System.out.println("  Smallest: " + jobIds.first());
        System.out.println("  Largest:  " + jobIds.last());

        // headSet — all IDs strictly less than "report-2"
        System.out.println("  IDs before 'report-2': " + jobIds.headSet("report-2"));
    }

    /**
     * Example 2 — TreeMap grouping job IDs by type.
     *
     * Maps job type names (String keys) to lists of job IDs.
     * TreeMap keeps keys in alphabetical order (DATA_SYNC, EMAIL, REPORT).
     * Shows: put(), get(), computeIfAbsent(), entrySet() in sorted key order.
     */
    private static void demoGroupedByType() {
        System.out.println("\n[TreeMap-2] Jobs grouped and sorted by type:");

        TreeMap<String, List<String>> grouped = new TreeMap<>();

        // Populate using computeIfAbsent for clean grouping
        addToGroup(grouped, JobType.EMAIL.name(),     "email-1");
        addToGroup(grouped, JobType.EMAIL.name(),     "email-2");
        addToGroup(grouped, JobType.REPORT.name(),    "report-1");
        addToGroup(grouped, JobType.DATA_SYNC.name(), "sync-1");
        addToGroup(grouped, JobType.DATA_SYNC.name(), "sync-2");
        addToGroup(grouped, JobType.DATA_SYNC.name(), "sync-fail-1");

        // Keys are iterated in sorted order (DATA_SYNC < EMAIL < REPORT)
        for (var entry : grouped.entrySet()) {
            System.out.println("  " + entry.getKey() + " (" +
                               entry.getValue().size() + " jobs): " + entry.getValue());
        }

        System.out.println("  First type key: " + grouped.firstKey());
        System.out.println("  Last type key:  " + grouped.lastKey());
    }

    private static void addToGroup(TreeMap<String, List<String>> map,
                                   String key, String jobId) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(jobId);
    }
}
