package org.example.easytable.util;

import org.example.easytable.common.aop.annotation.RedissonLock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DummyCounter {
    private final AtomicInteger counter = new AtomicInteger(0);

    @RedissonLock(key = "'counterLock'")
    @Transactional
    public void incrementCounter() {
        int currentValue = counter.get();
        System.out.println("current Counter value: " + currentValue);
        try {
            // 지연 시간을 주어 lock이 유지되고 있는 동안 다른 스레드들이 대기하도록 함
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        counter.set(currentValue + 1);
    }

    public int getCounter() {
        return counter.get();
    }
}
