package org.example.easytable.lock;

import org.example.easytable.common.utils.DummyCounter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RedissonLockTest {
    private final DummyCounter dummyCounter;

    @Autowired
    public RedissonLockTest(DummyCounter dummyCounter) {
        this.dummyCounter = dummyCounter;
    }

    @Test
    public void counterLockingTest() throws InterruptedException {
        int numThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                try {
                    dummyCounter.incrementCounter();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 모든 스레드가 정상적으로 작업을 마쳤다면, counter 값은 numThreads와 같아야 함
        assertEquals(numThreads, dummyCounter.getCounter());
    }
}
