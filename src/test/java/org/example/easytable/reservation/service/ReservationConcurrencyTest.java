package org.example.easytable.reservation.service;

import org.example.easytable.member.entity.Member;
import org.example.easytable.member.entity.MemberType;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// 실제로 발생할 수 있는 동시성 문제 검증을 위해 mock 객체 대신 실제 객체를 주입받음
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReservationConcurrencyTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Long restaurantId = 1L;
    private int remainingTableCount;
    private int threadCount;
    private Long memberId = 1L;

    @BeforeEach
    @Commit
    @Transactional
    public void init() {
        // ✅ 1. 기존 회원 ID(1L) 조회 → 없으면 생성
        Member member = memberRepository.findById(memberId)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .name("Test User")
                            .email("test@example.com")
                            .password("password")
                            .memberType(MemberType.USER) // ✅ 명시적으로 OWNER 지정
                            .build();
                    return memberRepository.save(newMember);
                });
        memberId = member.getId();

        // ✅ 2. 기존 레스토랑 ID(1L) 조회 → 없으면 생성 (Owner 설정 필수)
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseGet(() -> {
                    Restaurant newRestaurant = Restaurant.builder()
                            .name("Test Restaurant")
                            .address("123 Main St")
                            .maxTableCount(15)
                            .category(RestaurantCategory.KOREAN) // ✅ 임의로 카테고리 지정
                            .owner(member) // ✅ owner 필수
                            .build();
                    return restaurantRepository.save(newRestaurant);
                });



        // ✅ 필드 값 업데이트
        restaurantId = restaurant.getId();
        remainingTableCount = restaurant.getRemainingTableCount();
        threadCount = 30;
    }




    @Test
    public void checkReservationSaveConcurrency() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCnt = new AtomicInteger();
        AtomicInteger failCnt = new AtomicInteger();
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    barrier.await(); // 모든 스레드가 여기서 만남

                    ReservationCreateReqDto reqDto = new ReservationCreateReqDto(LocalDateTime.now());
                    reservationService.createReservation(restaurantId, memberId, reqDto);
                    results.add("✅ Thread-" + Thread.currentThread().getName() + " 성공");
                    successCnt.incrementAndGet();
                } catch (Exception e) {
                    results.add("❌ Thread-" + Thread.currentThread().getName() + " 실패: " + e.getMessage());
                    failCnt.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        doneLatch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 결과 출력
        System.out.println("\n=== 실행 결과 ===");
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i+1) + ". " + results.get(i));
        }
        System.out.println("================\n");

        // 결과 검증
        Restaurant targetRestaurant = restaurantRepository.findById(restaurantId).orElse(null);
        assertNotNull(targetRestaurant);

        int expectedRemainingTables = remainingTableCount - successCnt.get();
        assertEquals(expectedRemainingTables, targetRestaurant.getRemainingTableCount());

        int expectedFailures = Math.max(0, threadCount - remainingTableCount);
        int expectedSuccesses = Math.min(remainingTableCount, threadCount);

        System.out.println("✅ 예상 성공 스레드 개수: " + expectedSuccesses);
        System.out.println("✅ 예상 실패 스레드 개수: " + expectedFailures);
        System.out.println("🔹 실제 성공한 스레드 개수: " + successCnt.get());
        System.out.println("🔹 실제 실패한 스레드 개수: " + failCnt.get());

        assertEquals(expectedSuccesses, successCnt.get(), "✅ 성공한 스레드 개수가 예상과 다릅니다.");
        assertEquals(expectedFailures, failCnt.get(), "❌ 실패한 스레드 개수가 예상과 다릅니다.");
    }

//    @Test
//    public void checkReservationDeleteConcurrency() throws InterruptedException {
//        // given
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch startLatch = new CountDownLatch(1);
//        CountDownLatch doneLatch = new CountDownLatch(threadCount);
//        AtomicInteger successCnt = new AtomicInteger();
//        AtomicInteger failCnt = new AtomicInteger();
//
//        // when
//        for (int i = 0; i < threadCount; i++) {
//            executor.submit(() -> {
//                try {
//                    startLatch.await();
//                    ReservationCreateResDto currentReservation = reservationService.save(
//                        restaurantId, LocalDateTime.now(), guestCount, 1L);
//                    reservationService.deleteReservation(restaurantId, currentReservation.getReservationId());
//                    successCnt.incrementAndGet();
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                    failCnt.incrementAndGet();
//                } finally {
//                    doneLatch.countDown();
//                }
//            });
//        }
//
//        startLatch.countDown();
//
//        doneLatch.await();
//        executor.shutdown();
//
//        // then
//        Restaurant targetRestaurant = restaurantRepository.findById(restaurantId).orElse(null);
//
//        assertNotNull(targetRestaurant);
//        assertEquals(remainingTableCount, targetRestaurant.getValidSeatCount());
//        assertEquals(threadCount / guestCount, successCnt.intValue());
//        assertEquals(threadCount - (threadCount / guestCount), failCnt.intValue());
//    }
}
