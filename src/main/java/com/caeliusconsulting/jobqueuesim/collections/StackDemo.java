package com.caeliusconsulting.jobqueuesim.collections;

import com.caeliusconsulting.jobqueuesim.jobs.EmailJob;
import com.caeliusconsulting.jobqueuesim.jobs.Job;

import java.util.Stack;

/**
 * Demonstrates two Stack use-cases.
 *
 * Stack: LIFO (Last-In, First-Out) data structure.
 * Operations: push() → add to top, pop() → remove from top, peek() → view top.
 *
 * ⚠ NOTE: java.util.Stack is a legacy class that extends Vector (synchronized).
 *   In modern Java, prefer Deque / ArrayDeque for LIFO behaviour:
 *     Deque<String> stack = new ArrayDeque<>();
 *     stack.push("item");  // addFirst
 *     stack.pop();         // removeFirst
 *   Stack is used here because it is explicitly required by the syllabus.
 *
 * Syllabus: Stack, push/pop/peek, LIFO, legacy vs modern alternatives
 */
public class StackDemo {

    public static void runDemo() {
        System.out.println("\n=== Stack Demo ===");
        System.out.println("  NOTE: Prefer Deque/ArrayDeque over Stack in modern Java.");
        demoStatusTransitionStack();
        demoUndoStack();
    }

    /**
     * Example 1 — Job status transition stack.
     *
     * Models a job's lifecycle transitions pushed onto a stack so you can
     * walk backwards through history (e.g., for debugging or rollback display).
     * Shows: push(), peek(), pop(), isEmpty().
     */
    private static void demoStatusTransitionStack() {
        System.out.println("\n[Stack-1] Job status transition history:");

        Stack<String> transitions = new Stack<>();
        transitions.push("QUEUED");
        transitions.push("PROCESSING");
        transitions.push("COMPLETED");

        System.out.println("  Current status (peek): " + transitions.peek());
        System.out.println("  Stack (top→bottom): " + transitions);

        // Pop each status — walk backwards through the lifecycle
        System.out.println("  Unwinding transitions:");
        while (!transitions.isEmpty()) {
            System.out.println("    ← " + transitions.pop());
        }
    }

    /**
     * Example 2 — Job undo stack.
     *
     * Simulates an undo mechanism: jobs are pushed as they are dispatched,
     * and can be "undone" (popped) in reverse order.
     * Shows: push(), pop(), peek(), size().
     */
    private static void demoUndoStack() {
        System.out.println("\n[Stack-2] Dispatch undo stack:");

        Stack<Job> undoStack = new Stack<>();
        undoStack.push(new EmailJob("email-undo-1"));
        undoStack.push(new EmailJob("email-undo-2"));
        undoStack.push(new EmailJob("email-undo-3"));

        System.out.println("  Dispatched " + undoStack.size() + " jobs.");
        System.out.println("  Last dispatched (peek): " + undoStack.peek().getJobId());

        // Undo last dispatch
        Job undone = undoStack.pop();
        System.out.println("  Undid dispatch of: " + undone.getJobId());
        System.out.println("  Remaining in undo stack: " + undoStack.size());
    }
}
