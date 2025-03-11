package org.example.easytable.queueing;

import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.member.entity.Member;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.repository.legacy.ReservationQueueRepository;
import org.example.easytable.reservation.service.ReservationService;
import org.example.easytable.reservation.service.legacy.CreateReservationQueueService;
import org.example.easytable.restaurant.entity.Restaurant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
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
    private final Long MAX_PROCESSING_QUEUE_LENGTH = 100L;

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationQueueRepository reservationQueue;

    @Mock
    private SerializerUtil<ReservationCreateReqDto> serializer;

    // 테스트 대상 객체 (ReservationQueueRepository와 SerializerUtil이 주입됨)
    @InjectMocks
    private CreateReservationQueueService queueService;

    // Test for enqueueReservation
    @Test
    public void enqueueReservation_Success() {
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);
        String serializedJson = "serialized-request";
        // 직렬화 stub 설정
        when(serializer.serialize(request)).thenReturn(serializedJson);
        // waiting queue에 추가 시 true 반환 stub 설정
        when(reservationQueue.addToWaitingQueue(eq(serializedJson), anyDouble()))
                .thenReturn(Mono.just(true));

        Mono<Boolean> resultMono = queueService.enqueueReservation(request);

        StepVerifier.create(resultMono)
                .expectNext(true)
                .verifyComplete();

        // 요청에 해당하는 sink가 등록되었는지 확인
        assertTrue(queueService.getResultSinkMap().containsKey(request.getRequestId()));
    }

    // Test for cancelReservation
    @Test
    public void cancelReservation_Success() {
        String reservationId = "test-id";
        // JSON 문자열 (예약 id 포함)
        String json = "{\"id\":\"" + reservationId + "\", \"restaurantId\":1, \"memberId\":1}";
        Range<Long> range = Range.of(Bound.inclusive(0L), Bound.unbounded());
        when(reservationQueue.getWaitingQueueRange(range)).thenReturn(Flux.just(json));
        // JSON에서 id 추출 시 stub 설정
        when(serializer.getFromJson(json, "id")).thenReturn(reservationId);
        // waiting queue에서 제거 시 1L 반환 stub 설정
        when(reservationQueue.removeFromWaitingQueue(json)).thenReturn(Mono.just(1L));

        Mono<Boolean> resultMono = queueService.cancelReservation(reservationId);

        StepVerifier.create(resultMono)
                .expectNext(true)
                .verifyComplete();
    }

    // Test for waitForProcessingResult when sink exists
    @Test
    public void waitForProcessingResult_WithSink() {
        Long requestId = System.currentTimeMillis();
        // 테스트용 sink를 직접 등록
        Sinks.One<ReservationCreateResDto> sink = Sinks.one();
        queueService.getResultSinkMap().put(requestId, sink);

        ReservationCreateResDto dummyRes = new ReservationCreateResDto(
                requestId, 100L, 1L, 1L, LocalDateTime.now(), ReservationStatus.CONFIRMED);
        // sink에 결과 emit
        sink.tryEmitValue(dummyRes);

        Mono<ReservationCreateResDto> resultMono = queueService.waitForProcessingResult(requestId);

        StepVerifier.create(resultMono)
                .expectNext(dummyRes)
                .verifyComplete();
    }

    // Test for waitForProcessingResult when sink does not exist
    @Test
    public void waitForProcessingResult_NoSink() {
        Long requestId = System.currentTimeMillis();
        Mono<ReservationCreateResDto> resultMono = queueService.waitForProcessingResult(requestId);

        StepVerifier.create(resultMono)
                .verifyComplete(); // Empty Mono expected
    }

    // Test for processQueue (verifying the entire chain)
    @Test
    public void processQueue_Success() throws Exception {
        // 더미 예약 요청 및 직렬화된 JSON 생성
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);
        String serializedJson = "serialized-request";
        when(serializer.serialize(request)).thenReturn(serializedJson);

        // enqueueReservation 호출하여 sink가 등록되도록 함
        when(reservationQueue.addToWaitingQueue(eq(serializedJson), anyDouble()))
                .thenReturn(Mono.just(true));
        Mono<Boolean> enqueued = queueService.enqueueReservation(request);
        StepVerifier.create(enqueued)
                .expectNext(true)
                .verifyComplete();

        // processQueue() 호출 시 waiting queue 범위 및 제거 stub 설정
        Range<Long> waitingRange = Range.of(Bound.inclusive(0L), Bound.inclusive(MAX_PROCESSING_QUEUE_LENGTH - 1));
        when(reservationQueue.getWaitingQueueRange(waitingRange)).thenReturn(Flux.just(serializedJson));
        when(reservationQueue.removeFromWaitingQueue(serializedJson)).thenReturn(Mono.just(1L));

        // processing queue 관련 stub 설정
        when(reservationQueue.getProcessingQueueSize()).thenReturn(Mono.just(5L));
        when(reservationQueue.addToProcessingQueue(eq(serializedJson), anyDouble()))
                .thenReturn(Mono.just(true));
        when(reservationQueue.removeFromProcessingQueue(serializedJson)).thenReturn(Mono.just(1L));

        // JSON 역직렬화 stub 설정
        when(serializer.deserialize(serializedJson)).thenReturn(request);

        // 더미 Reservation 및 처리 결과 생성
        Member dummyMember = new Member();
        Restaurant dummyRestaurant = new Restaurant();
        Reservation dummyReservation = new Reservation(dummyMember, dummyRestaurant, LocalDateTime.now());
        dummyReservation.confirmReservation();
        ReservationCreateResDto resDto = ReservationCreateResDto.of(dummyReservation, request.getRequestId());

        // 예약 서비스 createReservation 호출 시 stub 설정
        when(reservationService.createReservation(any(ReservationCreateReqDto.class)))
                .thenReturn(resDto);

        // processQueue() 실행 (비동기 처리)
        queueService.processQueue();

        // waitForProcessingResult()로 처리 결과 검증
        Mono<ReservationCreateResDto> resultMono = queueService.waitForProcessingResult(request.getRequestId());
        StepVerifier.create(resultMono.timeout(Duration.ofSeconds(5)))
                .assertNext(result -> {
                    assertEquals(request.getRequestId(), result.requestId());
                })
                .verifyComplete();
    }
}
