// Reference appendix — demonstrates the major String method categories used elsewhere in this project (see JobType.java, LogFormatter.java for pool-vs-heap and StringBuilder usage in context).
package com.caeliusconsulting.jobqueuesim.appendix;

/** Runs small grouped demonstrations of the principal String APIs. */
public final class StringMethodsDemo {
    private StringMethodsDemo() {
    }
    /** Runs every String-method category in a readable order. */
    public static void main(String[] args) {
        inspect();
        compare();
        transform();
        splitAndSearch();
    }
    /** Demonstrates inspection methods. */
    private static void inspect() {
        String value = "queue-job";
        System.out.println("length=" + value.length() + ", charAt=" + value.charAt(0));
        System.out.println("substring=" + value.substring(6) + ", indexOf=" + value.indexOf('-'));
    }
    /** Demonstrates comparison methods. */
    private static void compare() {
        String value = "Job";
        System.out.println("equals=" + value.equals("Job") + ", ignoreCase="
                + value.equalsIgnoreCase("job") + ", compareTo=" + value.compareTo("Job"));
    }
    /** Demonstrates transformation methods. */
    private static void transform() {
        String value = " job ";
        System.out.println(value.trim().toUpperCase().replace('J', 'Q').concat("S"));
    }
    /** Demonstrates splitting, searching, and emptiness checks. */
    private static void splitAndSearch() {
        String value = "email,report";
        System.out.println("parts=" + value.split(",").length + ", contains="
                + value.contains("report") + ", empty=" + value.isEmpty());
    }
}
