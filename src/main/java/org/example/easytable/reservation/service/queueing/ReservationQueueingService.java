package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.repository.MessagePublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static reactor.core.publisher.Sinks.One;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationQueueingService {
    private final MessagePublisher publisher;
    private final SinksRegistry sinkRegistry;

    @Value("${redis.streams.producer.waiting-seconds:60}")
    private int waitingTime;

    public ReservationCreateResDto publishRequest(ReservationCreateReqMessage dto) throws Exception {
        One<ReservationCreateResDto> sink = addSink(dto);

        publisher.publish(dto);

        try {
            // 일정 시간 동안 Subscriber의 요청 처리 결과를 blocking 대기
            return sink.asMono().block(Duration.ofSeconds(waitingTime));
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof TimeoutException) {
                throw new TimeoutException();
            }
            throw e;
        }
    }

    private One<ReservationCreateResDto> addSink(ReservationCreateReqMessage dto) {
        One<ReservationCreateResDto> sink = Sinks.one();
        sinkRegistry.registerSink(dto.getRequestId(), sink);
        log.debug("saved sink: {}", sinkRegistry.getSink(dto.getRequestId()));
        return sink;
    }
}
