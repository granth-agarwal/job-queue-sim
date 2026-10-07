package com.caeliusconsulting.jobqueuesim.jobs;

/**
 * Lifecycle states for a Job.
 *
 * Transitions:
 *   QUEUED → PROCESSING → COMPLETED
 *                       ↘ FAILED
 *
 * Syllabus: Enums
 */
public enum JobStatus {
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED
}
