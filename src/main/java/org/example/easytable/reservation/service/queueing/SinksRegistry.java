package org.example.easytable.reservation.service.queueing;

import lombok.extern.slf4j.Slf4j;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

import static reactor.core.publisher.Sinks.One;

@Component
@Slf4j
public class SinksRegistry {
    private final ConcurrentHashMap<String, One<ReservationCreateResDto>> sinkMap = new ConcurrentHashMap<>();

    public void registerSink(String requestId, One<ReservationCreateResDto> sink) {
        sinkMap.put(requestId, sink);
    }

    public One<ReservationCreateResDto> getSink(String requestId) { return sinkMap.get(requestId); }

    public void completeSink(String requestId, ReservationCreateResDto response) {
        One<ReservationCreateResDto> sink = sinkMap.get(requestId);
        if (sink == null) {
            log.error("Request Id {}에 해당하는 Sinks가 존재하지 않습니다.", requestId);
            throw new IllegalArgumentException(requestId + "에 해당하는 Sinks가 존재하지 않습니다.");
        }

        sink.tryEmitValue(response);
        sinkMap.remove(requestId);
    }

    public void completeSinkExceptionally(String requestId, Throwable e) {
        Sinks.One<ReservationCreateResDto> sink = sinkMap.get(requestId);
        if (sink == null) { return; }

        sink.tryEmitError(e);
        sinkMap.remove(requestId);
    }
}
