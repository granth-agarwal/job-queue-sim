package com.caeliusconsulting.jobqueuesim.worker;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ExecutionHistory {
    private final Lock lock = new ReentrantLock();
    private final LinkedList<String> events = new LinkedList<>();

    public void record(String event) {
        lock.lock();
        try {
            events.add(event);
        } finally {
            lock.unlock();
        }
    }

    public LinkedList<String> snapshot() {
        lock.lock();
        try {
            return new LinkedList<>(events);
        } finally {
            lock.unlock();
        }
    }
}
