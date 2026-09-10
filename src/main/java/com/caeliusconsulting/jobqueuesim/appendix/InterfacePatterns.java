// Reference appendix — five short interface patterns for the review appendix. Not part of the core pipeline.
package com.caeliusconsulting.jobqueuesim.appendix;

public final class InterfacePatterns {
    private InterfacePatterns() {
    }

    interface Payable { double calculatePay(); }
    static class Employee implements Payable {
        private final double hourlyRate;
        private final int hoursWorked;
        Employee(double hourlyRate, int hoursWorked) {
            this.hourlyRate = hourlyRate;
            this.hoursWorked = hoursWorked;
        }
        public double calculatePay() { return hourlyRate * hoursWorked; }
    }

    interface Printable { void print(); }
    interface Storable { void store(); }
    static class Document implements Printable, Storable {
        private final StringBuffer activity = new StringBuffer();
        public void print() { activity.append("printed "); }
        public void store() { activity.append("stored"); }
        String activity() { return activity.toString(); }
    }

    interface Bounded { int MAX_ITEMS = 10; }
    static class Inventory implements Bounded {
        int remainingCapacity(int itemCount) { return Math.max(0, MAX_ITEMS - itemCount); }
    }

    interface Greeting {
        default String greet(String name) { return "Hello, " + name; }
    }
    static class ReviewerGreeting implements Greeting { }

    interface Converter<T> { T convert(String value); }
    static class IntegerConverter implements Converter<Integer> {
        public Integer convert(String value) { return Integer.valueOf(value); }
    }
}
