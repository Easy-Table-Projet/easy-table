package org.example.easytable.reservation.service;

import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

// 실제로 발생할 수 있는 동시성 문제 검증을 위해 mock 객체 대신 실제 객체를 주입받음
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReservationConcurrencyTest {
    private final ReservationService reservationService;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    private Long restaurantId;
    private int validSeatCount;
    private int threadCount;
    private int guestCount;

    @Autowired
    public ReservationConcurrencyTest(
            ReservationService reservationService,
            RestaurantRepository restaurantRepository,
            MemberRepository memberRepository
    ) {
        this.reservationService = reservationService;
        this.restaurantRepository = restaurantRepository;
        this.memberRepository = memberRepository;
    }

    @BeforeEach
    @Commit
    @Transactional
    public void init() {
        restaurantId = 1L;
        validSeatCount = 30;
        threadCount = 30;
        guestCount = 3;

//        restaurantRepository.save(Restaurant.newRestaurant(
//                new RestaurantCreateDto("target", "addr1", validSeatCount, "CHINESE")));
//
//        for(int i = 1; i <= threadCount; i++) {
//            memberRepository.save(Member.builder()
//                    .email("test" + i + "@mail.com")
//                    .name("test" + i)
//                    .password("password" + i)
//                    .address("addr" + i)
//                    .isDeleted(false)
//                    .build()
//            );
//        }
    }

    @Test
    @Transactional
    @Rollback
    public void checkReservationSaveConcurrency() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();
        AtomicInteger failCnt = new AtomicInteger();

        // when

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    reservationService.save(restaurantId, LocalDateTime.now(), guestCount, 1L);
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
    @Rollback
    public void checkReservationDeleteConcurrency() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();
        AtomicInteger failCnt = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ReservationCreateResDto currentReservation = reservationService.save(
                        restaurantId, LocalDateTime.now(), guestCount, 1L);
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
        Restaurant targetRestaurant = restaurantRepository.findById(restaurantId).orElse(null);

        assertNotNull(targetRestaurant);
        assertEquals(validSeatCount, targetRestaurant.getValidSeatCount());
        assertEquals(threadCount, successCnt.intValue());
        assertEquals(0, failCnt.intValue());
    }
}
