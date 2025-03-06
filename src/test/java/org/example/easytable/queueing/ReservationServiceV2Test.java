package org.example.easytable.queueing;

import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.service.legacy.CreateReservationQueueService;
import org.example.easytable.reservation.service.legacy.ReservationServiceV2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceV2Test {
    @Mock
    private CreateReservationQueueService queueService;

    @InjectMocks
    private ReservationServiceV2 reservationServiceV2;

    // 예약 성공 시 (enqueue 성공 후 처리 결과가 전달되는 경우)
    @Test
    public void submitReservation_Success() {
        // 더미 예약 요청 (ReservationPostReqDto는 null 혹은 dummy 인스턴스로 대체)
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);
        ReservationCreateResDto expectedRes = new ReservationCreateResDto(
                request.getRequestId(), 100L, 1L, 1L, LocalDateTime.now(), ReservationStatus.CONFIRMED);

        // enqueueReservation이 true를 반환하고, waitForProcessingResult에서 정상 결과를 Mono로 반환한다고 가정
        when(queueService.enqueueReservation(request)).thenReturn(Mono.just(true));
        when(queueService.waitForProcessingResult(request.getRequestId()))
                .thenReturn(Mono.just(expectedRes));

        Mono<ReservationCreateResDto> resultMono = reservationServiceV2.submitReservation(request);

        StepVerifier.create(resultMono)
                .expectNext(expectedRes)
                .verifyComplete();
    }

    // 예약 요청이 대기열 등록에 실패한 경우
    @Test
    public void submitReservation_EnqueueFailure() {
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);

        when(queueService.enqueueReservation(request)).thenReturn(Mono.just(false));

        Mono<ReservationCreateResDto> resultMono = reservationServiceV2.submitReservation(request);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("대기열 등록 실패"))
                .verify();
    }

    // 예약 처리 결과가 일정 시간 내에 도착하지 않아 타임아웃 되는 경우
    // (CustomException.of(ErrorCode.REQUEST_TIMEOUT)으로 매핑)
    @Test
    public void submitReservation_Timeout() {
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 1L, null);

        when(queueService.enqueueReservation(request)).thenReturn(Mono.just(true));
        // 결과가 전달되지 않음 (Mono.never())
        when(queueService.waitForProcessingResult(request.getRequestId())).thenReturn(Mono.never());

        // 가상시간(VirtualTime)을 이용하여 타임아웃을 검증
        StepVerifier.withVirtualTime(() -> reservationServiceV2.submitReservation(request))
                .thenAwait(Duration.ofSeconds(30))
                .expectErrorMatches(throwable ->
                        throwable instanceof CustomException &&
                                ((CustomException) throwable).getErrorCode() == ErrorCode.REQUEST_TIMEOUT)
                .verify();
    }
}
