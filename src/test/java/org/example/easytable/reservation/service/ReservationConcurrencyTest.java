package org.example.easytable.reservation.service;

import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 실제로 발생할 수 있는 동시성 문제 검증을 위해 mock 객체 대신 실제 객체를 주입받음
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReservationConcurrencyTest {
    private final ReservationService reservationService;
    private final RestaurantRepository restaurantRepository;

    @Autowired
    public ReservationConcurrencyTest(
        ReservationService reservationService, RestaurantRepository restaurantRepository
    ) {
        this.reservationService = reservationService;
        this.restaurantRepository = restaurantRepository;
    }

    @Test
    @Transactional
    public void checkReservationSaveConcurrency() throws InterruptedException {
        // given
        Long restaurantId = 1L;
        int validSeatCount = 30;
        int threadCount = 30;
        int guestCount = 3;
        Restaurant targetRestaurant = Restaurant.newRestaurant(
            new RestaurantCreateDto("target", "addr1", validSeatCount));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();
        AtomicInteger failCnt = new AtomicInteger();

        // when
        restaurantRepository.save(targetRestaurant);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.save(restaurantId, LocalDateTime.now(), guestCount);
                    successCnt.incrementAndGet();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    failCnt.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        doneLatch.await();
        executor.shutdown();

        // then
        assertEquals(validSeatCount / guestCount, successCnt.intValue());
        assertEquals(validSeatCount - (validSeatCount / guestCount), failCnt.intValue());
    }

    @Test
    @Transactional
    public void checkReservationDeleteConcurrency() throws InterruptedException {
        // given
        Long restaurantId = 1L;
        int validSeatCount = 30;
        int threadCount = 30;
        int guestCount = 3;
        Restaurant targetRestaurant = Restaurant.newRestaurant(
            new RestaurantCreateDto("target", "addr1", validSeatCount));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();
        AtomicInteger failCnt = new AtomicInteger();

        // when
        restaurantRepository.save(targetRestaurant);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ReservationCreateResDto currentReservation = reservationService.save(
                        restaurantId, LocalDateTime.now(), guestCount);
                    reservationService.deleteReservation(restaurantId, currentReservation.getReservationId());
                    successCnt.incrementAndGet();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    failCnt.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        doneLatch.await();
        executor.shutdown();

        // then
        assertEquals(validSeatCount, targetRestaurant.getValidSeatCount());
        assertEquals(threadCount, successCnt.intValue());
        assertEquals(0, failCnt.intValue());
    }
}
