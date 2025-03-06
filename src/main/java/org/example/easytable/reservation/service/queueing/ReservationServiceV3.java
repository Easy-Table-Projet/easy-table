package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.repository.MessagePublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class ReservationServiceV3 {
    private final MessagePublisher publisher;
    private final SinksRegistry sinkRegistry;

    public ReservationCreateResDto queueRequest(ReservationCreateReqDto dto) throws Exception {
        Sinks.One<ReservationCreateResDto> sink = Sinks.one();
        sinkRegistry.registerSink(dto.getRequestId(), sink);

        publisher.publish(dto);

        // 일정 시간 동안 Subscriber의 요청 처리 결과를 대기
        try {
            return sink.asMono().block(Duration.ofSeconds(5));
        } catch (Exception e) {
            sinkRegistry.completeSink(dto.getRequestId(), null);
            throw new TimeoutException("Request timed out");
        }
    }
}
