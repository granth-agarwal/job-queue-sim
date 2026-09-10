package com.caeliusconsulting.jobqueuesim;

public final class JobType {
    public static final String EMAIL = "EMAIL_JOB";
    public static final String REPORT = "REPORT_JOB";
    public static final String DATA_SYNC = "DATA_SYNC_JOB";

    public static final String EMAIL_HEAP_COPY = new String("EMAIL_JOB");

    private JobType() {
    }

    public static boolean isValidType(String type) {
        return EMAIL.equals(type) || REPORT.equals(type) || DATA_SYNC.equals(type);
    }
}
