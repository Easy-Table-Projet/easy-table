package org.example.easytable.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DummyCounter {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Transactional
    public void incrementCounter() {
        int currentValue = counter.get();
        System.out.println("Current Counter value: " + currentValue);
        counter.set(currentValue + 1);
    }

    public int getCounter() {
        return counter.get();
    }
}