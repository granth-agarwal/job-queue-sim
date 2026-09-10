// Reference appendix — five short abstract-class patterns for the review appendix. Not part of the core pipeline.
package com.caeliusconsulting.jobqueuesim.appendix;

/** Collects compact, independent examples of common abstract-class patterns. */
public final class AbstractClassPatterns {
    private AbstractClassPatterns() {
    }
    /** Combines implemented and unimplemented behavior. */
    abstract static class Partial {
        String label() { return "partial"; }
        abstract int value();
    }
    /** Shows that an abstract base may initialize inherited state. */
    abstract static class ConstructorBase {
        private final String name;
        ConstructorBase(String name) { this.name = name; }
        String name() { return name; }
        abstract void use();
    }
    /** Fixes an algorithm's steps while delegating one step to subclasses. */
    abstract static class TemplateProcessor {
        final void process() { before(); executeStep(); after(); }
        void before() { }
        abstract void executeStep();
        void after() { }
    }
    /** Mixes class-wide utility behavior with required instance behavior. */
    abstract static class StaticAbstractMix {
        static String category() { return "mixed"; }
        abstract String details();
    }
    /** Supplies reusable field-backed behavior to subclasses. */
    abstract static class FieldBacked {
        private int count;
        void increment() { count++; }
        int count() { return count; }
    }
}
