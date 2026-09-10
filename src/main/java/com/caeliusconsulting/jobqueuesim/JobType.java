package com.caeliusconsulting.jobqueuesim;

/** Holds the supported job-type strings and their validation helper. */
public final class JobType {
    public static final String EMAIL = "EMAIL_JOB";
    public static final String REPORT = "REPORT_JOB";
    public static final String DATA_SYNC = "DATA_SYNC_JOB";

    // Unlike the literals above, new forces a distinct String object on the heap.
    public static final String EMAIL_HEAP_COPY = new String("EMAIL_JOB");

    private JobType() {
    }

    /** Checks membership using value equality rather than reference equality. */
    public static boolean isValidType(String type) {
        return EMAIL.equals(type) || REPORT.equals(type) || DATA_SYNC.equals(type);
    }
}
