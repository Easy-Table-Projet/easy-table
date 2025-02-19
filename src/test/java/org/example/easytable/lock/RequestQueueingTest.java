package org.example.easytable.lock;

import org.example.easytable.config.MockRequestQueue;
import org.example.easytable.config.QueueingTestConfig;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDtoImpl;
import org.example.easytable.reservation.dto.request.ReservationGetByRestaurantReqDtoImpl;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.RequestFutureStore;
import org.example.easytable.reservation.service.RequestQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {QueueingTestConfig.class})
public class RequestQueueingTest {
    private final RequestQueue collectionQueue;
    private final RequestQueue redisQueue;
    private final MockRequestQueue mockRequestQueue;

    private final Long restaurantId = 1L;
    private final Long memberId = 1L;
    private final ReservationPostReqDto postReqDto = new ReservationPostReqDto(LocalDateTime.now());

    @Value("${collectionQueue-capacity:25}")
    private int capacity;
    private int threadCount;
    private RequestFutureStore requestFutureStore;

    @Autowired
    public RequestQueueingTest(
            @Qualifier("mockQueue") MockRequestQueue mockRequestQueue,
            @Qualifier("collectionQueue") RequestQueue collectionQueue,
            @Qualifier("redisQueue") RequestQueue redisQueue
    ) {
        this.mockRequestQueue = mockRequestQueue;
        this.collectionQueue = collectionQueue;
        this.redisQueue = redisQueue;
    }

    @BeforeEach
    public void init() {
        requestFutureStore = new RequestFutureStore();
    }

    @Test
    public void enqueueTest() throws InterruptedException {
        // given
        threadCount = 100;

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    mockRequestQueue.enqueue(new ReservationCreateReqDtoImpl(
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
        assertEquals(capacity, successCnt.intValue(), mockRequestQueue.getQueueSize());
    }

    @Test
    public void getRequestTest() {
        // given
        String uuid = UUID.randomUUID().toString();
        CompletableFuture<List<ReservationGetResDto>> future = new CompletableFuture<>();

        // when
        requestFutureStore.registerFuture(uuid, future);

        // then
        assertEquals(future, requestFutureStore.getFuture(uuid));
    }

    @Test
    public void processCollectionQueueTest() throws InterruptedException {
        // given
        threadCount = 250;
        Long restaurantId = 1L;

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();
        AtomicInteger foundReservationCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    String requestId = UUID.randomUUID().toString();
                    CompletableFuture<List<ReservationGetResDto>> future = new CompletableFuture<>();
                    requestFutureStore.registerFuture(requestId, future);
                    System.out.println(requestFutureStore.getFuture(requestId).toString());

                    if (!collectionQueue.enqueue(
                            new ReservationGetByRestaurantReqDtoImpl(restaurantId, requestId))
                    ) {
                        throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
                    }
                    List<ReservationGetResDto> foundReservations = future.get();
                    System.out.println("current future:" + future.toString() +
                        " successfully got future: " + (future == requestFutureStore.getFuture(requestId)));
                    successCnt.incrementAndGet();
                    foundReservationCount.addAndGet(foundReservations.size());
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
        // 큐를 대기하도록 구현하는 경우 threadCount와 비교하도록 수정할 것
        assertEquals(capacity, successCnt.intValue());
        assertEquals(0, foundReservationCount.intValue());
    }

    @Test
    public void processRedisQueueTest() throws InterruptedException {
        // given
        threadCount = 250;
        Long restaurantId = 1L;

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();
        AtomicInteger foundReservationCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                String requestId = UUID.randomUUID().toString();
                CompletableFuture<List<ReservationGetResDto>> future = new CompletableFuture<>();
                System.out.println("generated requestId: " + requestId);

                try {
                    requestFutureStore.registerFuture(requestId, future);
                    System.out.println(requestFutureStore.getFuture(requestId).toString());

                    if (!redisQueue.enqueue(new ReservationGetByRestaurantReqDtoImpl(restaurantId, requestId))) {
                        throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
                    }

                    redisQueue.processQueue();

                    List<ReservationGetResDto> foundReservations = future.get();
                    System.out.println("current future:" + future +
                        " successfully got future: " + (future == requestFutureStore.getFuture(requestId)));
                    successCnt.incrementAndGet();
                    foundReservationCount.addAndGet(foundReservations.size());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    future.completeExceptionally(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        // 큐를 대기하도록 구현하는 경우 threadCount와 비교하도록 수정할 것
        assertEquals(capacity, successCnt.intValue());
        assertEquals(0, foundReservationCount.intValue());
    }
}
