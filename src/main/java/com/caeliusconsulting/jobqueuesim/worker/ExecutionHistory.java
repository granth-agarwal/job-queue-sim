package com.caeliusconsulting.jobqueuesim.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ExecutionHistory {
    private final Lock lock = new ReentrantLock();
    private final List<String> events = new ArrayList<>();

    public void record(String event) {
        lock.lock();
        try {
            events.add(event);
        } finally {
            lock.unlock();
        }
    }

    public List<String> snapshot() {
        lock.lock();
        try {
            return new ArrayList<>(events);
        } finally {
            lock.unlock();
        }
    }
}
