// Reference appendix — five short abstract-class patterns for the review appendix. Not part of the core pipeline.
package com.caeliusconsulting.jobqueuesim.appendix;

public final class AbstractClassPatterns {
    private AbstractClassPatterns() {
    }

    abstract static class Partial {
        String label() { return getClass().getSimpleName(); }
        abstract int value();
    }
    static final class CompletePartial extends Partial {
        int value() { return label().length(); }
    }

    abstract static class ConstructorBase {
        private final String name;
        ConstructorBase(String name) { this.name = name; }
        String name() { return name; }
        abstract String use();
    }
    static final class NamedAction extends ConstructorBase {
        NamedAction(String name) { super(name); }
        String use() { return name().toUpperCase(); }
    }

    abstract static class TemplateProcessor {
        final String process() { return "prepare -> " + executeStep() + " -> finish"; }
        abstract String executeStep();
    }
    static final class SyncProcessor extends TemplateProcessor {
        String executeStep() { return "synchronize"; }
    }

    abstract static class StaticAbstractMix {
        static String category() { return StaticAbstractMix.class.getSimpleName(); }
        abstract String details();
    }
    static final class MixedDetail extends StaticAbstractMix {
        String details() { return category().toLowerCase(); }
    }

    abstract static class FieldBacked {
        private int count;
        void increment() { count++; }
        int count() { return count; }
    }
    static final class Counter extends FieldBacked { }
}
