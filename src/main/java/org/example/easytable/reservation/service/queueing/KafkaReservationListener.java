package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaReservationListener {

    private final SinksRegistry sinkRegistry;
    private final ReservationService reservationProcessingService;

    @KafkaListener(topics = "${kafka.topic.reservation:create-reservation}", groupId = "reservation-group")
    public void listen(ReservationCreateReqDto request) {
        try {
            ReservationCreateResDto response = reservationProcessingService.createReservation(request);
            // SinkRegistry를 통해 요청을 보낸 스레드에 결과 전송
            sinkRegistry.completeSink(request.getRequestId(), response);
        } catch (Exception e) {
            log.error(e.getMessage());
            sinkRegistry.completeSinkExceptionally(request.getRequestId(), e);
        }
    }
}
