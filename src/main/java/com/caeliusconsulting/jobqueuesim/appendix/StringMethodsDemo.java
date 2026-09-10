// Reference appendix — demonstrates the major String method categories used elsewhere in this project (see JobType.java, LogFormatter.java for pool-vs-heap and StringBuilder usage in context).
package com.caeliusconsulting.jobqueuesim.appendix;

public final class StringMethodsDemo {
    private StringMethodsDemo() {
    }
    public static void main(String[] args) {
        inspect();
        compare();
        transform();
        splitAndSearch();
    }
    private static void inspect() {
        String value = "queue-job";
        System.out.println("length=" + value.length() + ", charAt=" + value.charAt(0));
        System.out.println("substring=" + value.substring(6) + ", indexOf=" + value.indexOf('-'));
    }
    private static void compare() {
        String value = "Job";
        System.out.println("equals=" + value.equals("Job") + ", ignoreCase="
                + value.equalsIgnoreCase("job") + ", compareTo=" + value.compareTo("Job"));
    }
    private static void transform() {
        String value = " job ";
        System.out.println(value.trim().toUpperCase().replace('J', 'Q').concat("S"));
    }
    private static void splitAndSearch() {
        String value = "email,report";
        System.out.println("parts=" + value.split(",").length + ", contains="
                + value.contains("report") + ", empty=" + value.isEmpty());
    }
}
