package org.example.easytable.lock.queueing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.easytable.member.entity.Member;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.service.ReservationService;
import org.example.easytable.reservation.service.queueing.CreateReservationQueueService;
import org.example.easytable.restaurant.entity.Restaurant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateReservationQueueServiceTest {
    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReservationService reservationService;

    // zset 작업용 mock – redisTemplate.opsForZSet()에서 반환될 객체
    @Mock
    private ReactiveZSetOperations<String, String> zSetOps;

    // 테스트 대상 객체
    @InjectMocks
    private CreateReservationQueueService queueService;

    // Test for enqueueReservation
    @Test
    public void enqueueReservation_Success() {
        // Stubbing redisTemplate.opsForZSet() only for this test
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);
        // Assuming add() in Redis returns true
        when(zSetOps.add(eq("reservation:waiting"), any(String.class), anyDouble()))
                .thenReturn(Mono.just(true));

        Mono<Boolean> resultMono = queueService.enqueueReservation(request);

        StepVerifier.create(resultMono)
                .expectNext(true)
                .verifyComplete();

        // Verify that the sink is registered in resultSinkMap (assuming there's a getter)
        assertTrue(queueService.getResultSinkMap().containsKey(request.getRequestId()));
    }

    // Test for cancelReservation
    @Test
    public void cancelReservation_Success() {
        // Stubbing redisTemplate.opsForZSet() only for this test
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        String reservationId = "test-id";
        // JSON string of the reservation request (must include the field "id")
        String json = "{\"id\":\"" + reservationId + "\", \"restaurantId\":1, \"memberId\":1}";
        Range<Long> range = Range.of(Bound.inclusive(0L), Bound.unbounded());
        when(zSetOps.range("reservation:waiting", range)).thenReturn(Flux.just(json));
        when(zSetOps.remove("reservation:waiting", json)).thenReturn(Mono.just(1L));

        Mono<Boolean> resultMono = queueService.cancelReservation(reservationId);

        StepVerifier.create(resultMono)
                .expectNext(true)
                .verifyComplete();
    }

    // Test for waitForProcessingResult when sink exists
    @Test
    public void waitForProcessingResult_WithSink() {
        String requestId = "dummy-request";
        // Directly register a sink in resultSinkMap for testing
        Sinks.One<ReservationCreateResDto> sink = Sinks.one();
        queueService.getResultSinkMap().put(requestId, sink);

        ReservationCreateResDto dummyRes = new ReservationCreateResDto(
                requestId, 100L, 1L, 1L, LocalDateTime.now(), ReservationStatus.CONFIRMED);
        // Emit a value into the sink
        sink.tryEmitValue(dummyRes);

        Mono<ReservationCreateResDto> resultMono = queueService.waitForProcessingResult(requestId);

        StepVerifier.create(resultMono)
                .expectNext(dummyRes)
                .verifyComplete();
    }

    // Test for waitForProcessingResult when sink does not exist
    @Test
    public void waitForProcessingResult_NoSink() {
        String requestId = "non-existent";
        Mono<ReservationCreateResDto> resultMono = queueService.waitForProcessingResult(requestId);

        StepVerifier.create(resultMono)
                .verifyComplete(); // Empty Mono expected
    }

    // Test for processQueue (verifying the entire chain)
    @Test
    public void processQueue_Success() throws Exception {
        // redisTemplate의 opsForZSet() 스터빙
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        // 더미 예약 요청 생성
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(request);

        // 대기 큐 관련 스터빙
        when(zSetOps.range(eq("reservation:waiting"), any(Range.class)))
                .thenReturn(Flux.just(json));
        when(zSetOps.remove("reservation:waiting", json)).thenReturn(Mono.just(1L));
        when(zSetOps.add(eq("reservation:waiting"), eq(json), anyDouble()))
                .thenReturn(Mono.just(true)); // enqueueReservation() 내부 호출
        when(zSetOps.add(eq("reservation:processing"), eq(json), anyDouble()))
                .thenReturn(Mono.just(true));
        when(zSetOps.remove("reservation:processing", json)).thenReturn(Mono.just(1L));
        when(zSetOps.size("reservation:processing")).thenReturn(Mono.just(5L));

        // 더미 Reservation 생성 (실제 객체로 생성)
        Member dummyMember = new Member();
        Restaurant dummyRestaurant = new Restaurant();
        Reservation dummyReservation = new Reservation(dummyMember, dummyRestaurant, LocalDateTime.now());
        dummyReservation.confirmReservation();
        ReservationCreateResDto resDto = ReservationCreateResDto.of(dummyReservation, request.getRequestId());

        // 예약 서비스가 createReservation 호출 시 미리 생성한 resDto 반환
        when(reservationService.createReservation(any(ReservationCreateReqDto.class)))
                .thenReturn(resDto);

        // enqueueReservation()을 호출하여 resultSinkMap에 sink가 등록되도록 함
        Mono<Boolean> enqueued = queueService.enqueueReservation(request);
        StepVerifier.create(enqueued)
                .expectNext(true)
                .verifyComplete();

        // processQueue() 실행
        queueService.processQueue();

        // waitForProcessingResult()를 통해 처리 결과를 검증
        Mono<ReservationCreateResDto> resultMono = queueService.waitForProcessingResult(request.getRequestId());
        StepVerifier.create(resultMono.timeout(Duration.ofSeconds(5)))
                .assertNext(result -> {
                    assertEquals(request.getRequestId(), result.requestId());
                })
                .verifyComplete();
    }
}
