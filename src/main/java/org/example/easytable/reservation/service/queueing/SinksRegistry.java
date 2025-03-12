package org.example.easytable.reservation.service.queueing;

import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

import static reactor.core.publisher.Sinks.One;

@Component
public class SinksRegistry {
    private final ConcurrentHashMap<String, One<ReservationCreateResDto>> sinksMap = new ConcurrentHashMap<>();

    public void registerSink(String requestId, One<ReservationCreateResDto> sink) {
        sinksMap.put(requestId, sink);
    }

    public One<ReservationCreateResDto> getSink(String requestId) { return sinksMap.get(requestId); }

    public void completeSink(String requestId, ReservationCreateResDto response) {
        One<ReservationCreateResDto> sink = sinksMap.get(requestId);
        if (sink == null) {
            throw new IllegalArgumentException(requestId + "에 해당하는 Sinks가 존재하지 않습니다.");
        }

        sink.tryEmitValue(response);
        sinksMap.remove(requestId);
    }

    public void completeSinkExceptionally(String requestId, Throwable e) {
        Sinks.One<ReservationCreateResDto> sink = sinksMap.get(requestId);
        if (sink == null) { return; }

        sink.tryEmitError(e);
        sinksMap.remove(requestId);
    }
}
