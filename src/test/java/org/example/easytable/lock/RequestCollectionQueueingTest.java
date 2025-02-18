package org.example.easytable.lock;

import org.example.easytable.config.MockRequestQueue;
import org.example.easytable.config.QueueingTestConfig;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {QueueingTestConfig.class})
public class RequestCollectionQueueingTest {
    private final MockRequestQueue requestQueue;

    private final Long restaurantId = 1L;
    private final Long memberId = 1L;
    private final ReservationPostReqDto postReqDto = new ReservationPostReqDto(LocalDateTime.now());

    @Value("${queue-capacity:25}")
    private int capacity;

    @Autowired
    public RequestCollectionQueueingTest(MockRequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    @Test
    public void enqueueTest() throws InterruptedException {
        // given
        int threadCount = 100;

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    requestQueue.enqueue(new ReservationCreateReqDto(
                            restaurantId, memberId, postReqDto));
                    successCnt.incrementAndGet();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertEquals(capacity, requestQueue.getQueueSize());
        assertEquals(capacity, successCnt.intValue());
    }
}
