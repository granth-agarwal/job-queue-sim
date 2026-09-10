// Reference appendix — five short interface patterns for the review appendix. Not part of the core pipeline.
package com.caeliusconsulting.jobqueuesim.appendix;

/** Collects compact, independent examples of common interface patterns. */
public final class InterfacePatterns {
    private InterfacePatterns() {
    }

    /** A basic single-method contract. */
    interface Payable { double calculatePay(); }
    /** A concrete basic-contract implementation. */
    static class Employee implements Payable {
        public double calculatePay() { return 100.0; }
    }

    /** One of two unrelated capabilities. */
    interface Printable { void print(); }
    /** Another unrelated capability. */
    interface Storable { void store(); }
    /** A class can implement multiple interfaces. */
    static class Document implements Printable, Storable {
        public void print() { }
        public void store() { }
    }

    /** Interface fields are implicitly public, static, and final. */
    interface Bounded { int MAX_ITEMS = 10; }

    /** Interfaces may provide reusable default behavior. */
    interface Greeting {
        default String greet() { return "Hello"; }
    }

    /** Generic interfaces make a capability type-safe and reusable. */
    interface Converter<T> { T convert(String value); }
    /** A concrete generic-interface specialization. */
    static class IntegerConverter implements Converter<Integer> {
        public Integer convert(String value) { return Integer.valueOf(value); }
    }
}
