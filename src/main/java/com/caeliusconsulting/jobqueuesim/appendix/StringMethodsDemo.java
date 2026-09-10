// Reference appendix — demonstrates String behavior separately from the core pipeline.
package com.caeliusconsulting.jobqueuesim.appendix;

public final class StringMethodsDemo {
    private StringMethodsDemo() {
    }
    public static void main(String[] args) {
        String input = args.length > 0 && !args[0].isEmpty() ? args[0] : "Job";
        String searchTerm = args.length > 1 ? args[1] : "report";
        String referenceValue = args.length > 2
                ? args[2].intern() : new String("pooled-value");
        poolAndHeap(referenceValue);
        inspect(input);
        compare(input);
        compare("Queue");
        transform(input);
        splitAndSearch("email,report", searchTerm);
        splitAndSearch("", searchTerm);
    }
    private static void poolAndHeap(String comparisonValue) {
        String pooledValue = "pooled-value";
        System.out.println("sameReference=" + (pooledValue == comparisonValue)
                + ", sameValue=" + pooledValue.equals(comparisonValue));
    }
    private static void inspect(String value) {
        System.out.println("length=" + value.length() + ", charAt=" + value.charAt(0));
        System.out.println("substring=" + value.substring(1) + ", indexOf=" + value.indexOf('-'));
    }
    private static void compare(String value) {
        System.out.println("equals=" + value.equals("Job") + ", ignoreCase="
                + value.equalsIgnoreCase("job") + ", compareTo=" + value.compareTo("Job"));
    }
    private static void transform(String input) {
        String value = " " + input + " ";
        System.out.println(value.trim().toUpperCase().replace('J', 'Q').concat("S"));
    }
    private static void splitAndSearch(String value, String searchTerm) {
        System.out.println("parts=" + value.split(",").length + ", contains="
                + value.contains(searchTerm) + ", empty=" + value.isEmpty());
    }
}
