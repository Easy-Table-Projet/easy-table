package org.example.easytable.reservation.service.queueing;

import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SinksRegistry {
    private final ConcurrentHashMap<Long, Sinks.One<ReservationCreateResDto>> sinkMap = new ConcurrentHashMap<>();

    public void registerSink(long requestId, Sinks.One<ReservationCreateResDto> sink) {
        sinkMap.put(requestId, sink);
    }

    public void completeSink(long requestId, ReservationCreateResDto response) {
        Sinks.One<ReservationCreateResDto> sink = sinkMap.remove(requestId);
        if (sink == null) {
            throw new IllegalArgumentException(requestId + "를 갖는 요청이 존재하지 않습니다.");
        }

        sink.tryEmitValue(response);
    }
}
