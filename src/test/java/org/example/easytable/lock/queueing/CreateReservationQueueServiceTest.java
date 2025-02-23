package org.example.easytable.lock.queueing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.example.easytable.member.entity.Member;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.service.ReservationService;
import org.example.easytable.reservation.service.queueing.CreateReservationQueueService;
import org.example.easytable.restaurant.entity.Restaurant;
import org.junit.jupiter.api.BeforeEach;
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

    // 테스트 편의를 위해 resultSinkMap 접근용 getter가 있다고 가정합니다.
    // (없다면 Reflection을 사용하거나 패키지‑레벨 접근하도록 테스트 클래스를 동일 패키지에 두세요.)
    @BeforeEach
    public void setup() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    // enqueueReservation 테스트
    @Test
    public void enqueueReservation_Success() {
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);
        // redis에 JSON을 저장하는 add()가 true를 반환한다고 가정
        when(zSetOps.add(eq("reservation:waiting"), any(String.class), anyDouble()))
                .thenReturn(Mono.just(true));

        Mono<Boolean> resultMono = queueService.enqueueReservation(request);

        StepVerifier.create(resultMono)
                .expectNext(true)
                .verifyComplete();

        // 내부적으로 resultSinkMap에 sink가 등록되었는지 (테스트용 getter로 확인)
        assertTrue(queueService.getResultSinkMap().containsKey(request.getRequestId()));
    }

    // cancelReservation 테스트
    @Test
    public void cancelReservation_Success() {
        String reservationId = "test-id";
        // 예약 요청 JSON 문자열 (필요한 필드 "id"가 포함되어야 함)
        String json = "{\"id\":\"" + reservationId + "\", \"restaurantId\":1, \"memberId\":1}";
        Range<Long> range = Range.of(Bound.inclusive(0L), Bound.unbounded());
        when(zSetOps.range("reservation:waiting", range)).thenReturn(Flux.just(json));
        when(zSetOps.remove("reservation:waiting", json)).thenReturn(Mono.just(1L));

        Mono<Boolean> resultMono = queueService.cancelReservation(reservationId);

        StepVerifier.create(resultMono)
                .expectNext(true)
                .verifyComplete();
    }

    // waitForProcessingResult (sink 존재하는 경우)
    @Test
    public void waitForProcessingResult_WithSink() {
        String requestId = "dummy-request";
        // 테스트를 위해 resultSinkMap에 sink를 직접 등록 (getter가 있다고 가정)
        Sinks.One<ReservationCreateResDto> sink = Sinks.one();
        queueService.getResultSinkMap().put(requestId, sink);

        ReservationCreateResDto dummyRes = new ReservationCreateResDto(
                requestId, 100L, 1L, 1L, LocalDateTime.now(), ReservationStatus.CONFIRMED);
        // sink에 값을 emit
        sink.tryEmitValue(dummyRes);

        Mono<ReservationCreateResDto> resultMono = queueService.waitForProcessingResult(requestId);

        StepVerifier.create(resultMono)
                .expectNext(dummyRes)
                .verifyComplete();
    }

    // waitForProcessingResult (sink 미존재)
    @Test
    public void waitForProcessingResult_NoSink() {
        String requestId = "non-existent";
        Mono<ReservationCreateResDto> resultMono = queueService.waitForProcessingResult(requestId);

        StepVerifier.create(resultMono)
                .verifyComplete(); // 빈 Mono
    }

    // processQueue 테스트 (전체 체인 검증)
    @Test
    public void processQueue_Success() throws Exception {
        // 더미 예약 요청 생성
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);
        // ObjectMapper를 사용하여 JSON 문자열 생성 (serialize()와 deserialize()가 동일하게 동작한다고 가정)
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(request);

        // waiting queue에서 json이 조회되도록 설정
        when(zSetOps.range(eq("reservation:waiting"), any(Range.class)))
                .thenReturn(Flux.just(json));
        // waiting queue에서 해당 json 제거 시 1L 반환
        when(zSetOps.remove("reservation:waiting", json)).thenReturn(Mono.just(1L));
        // processing queue에 추가 시 성공 반환
        when(zSetOps.add(eq("reservation:processing"), eq(json), anyDouble()))
                .thenReturn(Mono.just(true));
        // processing queue에서 제거 시 1L 반환
        when(zSetOps.remove("reservation:processing", json)).thenReturn(Mono.just(1L));

        // 테스트를 위해 resultSinkMap에 sink 등록 (getter 사용)
        Sinks.One<ReservationCreateResDto> sink = Sinks.one();
        queueService.getResultSinkMap().put(request.getRequestId(), sink);

        // 예약 서비스가 createReservation 호출 시 dummy Reservation을 반환하도록 설정
        Reservation dummyReservation = mock(Reservation.class);
        Member dummyMember = mock(Member.class);
        Restaurant dummyRestaurant = mock(Restaurant.class);
        when(dummyReservation.getId()).thenReturn(123L);
        when(dummyReservation.getMember()).thenReturn(dummyMember);
        when(dummyReservation.getRestaurant()).thenReturn(dummyRestaurant);
        when(dummyReservation.getReservationTime()).thenReturn(LocalDateTime.now());
        when(dummyReservation.getStatus()).thenReturn(ReservationStatus.CONFIRMED);
        when(dummyMember.getId()).thenReturn(1L);
        when(dummyRestaurant.getId()).thenReturn(1L);

        when(reservationService.createReservation(any(ReservationCreateReqDto.class)))
                .thenReturn(ReservationCreateResDto.from(dummyReservation));

        // processQueue()는 내부에서 subscribe()를 호출하므로, 직접 호출
        queueService.processQueue();

        // sink에 emit된 결과를 검증
        StepVerifier.create(sink.asMono())
                .assertNext(result -> {
                    assertEquals(request.getRequestId(), result.requestId());
                    assertEquals(dummyReservation.getId(), result.reservationId());
                    // 필요에 따라 추가 검증 (memberId, restaurantId 등)
                })
                .verifyComplete();
    }
}
