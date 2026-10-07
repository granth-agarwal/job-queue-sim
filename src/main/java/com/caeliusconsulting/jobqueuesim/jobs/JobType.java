package com.caeliusconsulting.jobqueuesim.jobs;

/**
 * Job types used for routing by the dispatcher.
 *
 * Syllabus: Enums
 */
public enum JobType {
    EMAIL,
    REPORT,
    DATA_SYNC,
    UNKNOWN   // fallback for unrecognised types
}
