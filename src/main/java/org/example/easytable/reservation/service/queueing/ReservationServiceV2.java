package org.example.easytable.reservation.service.queueing;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReservationServiceV2 {
    private final CreateReservationQueueService queueService;

    public Mono<ReservationCreateResDto> submitReservation(ReservationCreateReqDto request) {
        return queueService.enqueueReservation(request)
                .flatMap(success -> {
                    if (success) {
                        // 예약 요청에 대한 처리 결과를 기다림 (Sinks에 의해 완료될 때까지)
                        return queueService.waitForProcessingResult(request.getRequestId())
                                // 만약 일정 시간 내에 결과가 없으면 타임아웃 처리 (선택 사항)
                                .timeout(Duration.ofSeconds(30))
                                .onErrorMap(throwable -> CustomException.of(
                                        ErrorCode.REQUEST_TIMEOUT));
                    } else {
                        return Mono.error(new RuntimeException("대기열 등록 실패"));
                    }
                });
    }

    // 수정 예정
//    public Mono<ReservationCreateResDto> getReservationStatus(String id) {
//        ReservationCreateResDto response = new ReservationCreateResDto();
//        response.setId(id);
//        response.setStatus("COMPLETED");
//        response.setMessage("Reservation has been processed.");
//        return Mono.just(response);
//    }
//
//    public Mono<ReservationCreateResDto> cancelReservation(String id) {
//        return queueService.cancelReservation(id)
//                .map(success -> {
//                    ReservationCreateResDto response = new ReservationCreateResDto();
//                    response.setId(id);
//                    if(success) {
//                        response.setStatus("CANCELLED");
//                        response.setMessage("Reservation cancelled.");
//                    } else {
//                        response.setStatus("NOT_FOUND");
//                        response.setMessage("Reservation not found or already processed.");
//                    }
//                    return response;
//                });
//    }
}
