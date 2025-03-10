package org.example.easytable.reservation.service.queueing;

import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;

import static reactor.core.publisher.Sinks.One;

@Component
public class SinksRegistry {
    private final ConcurrentHashMap<Long, One<ReservationCreateResDto>> sinkMap = new ConcurrentHashMap<>();

    public void registerSink(long requestId, One<ReservationCreateResDto> sink) {
        sinkMap.put(requestId, sink);
    }

    public One<ReservationCreateResDto> getSink(long requestId) { return sinkMap.get(requestId); }

    public void completeSink(long requestId, ReservationCreateResDto response) {
        One<ReservationCreateResDto> sink = sinkMap.get(requestId);
        if (sink == null) { throw new IllegalArgumentException(requestId + "에 해당하는 Sinks가 존재하지 않습니다."); }

        sink.tryEmitValue(response);
        sinkMap.remove(requestId);
    }

    public void completeSinkExceptionally(long requestId, Throwable e) {
        Sinks.One<ReservationCreateResDto> sink = sinkMap.get(requestId);
        if (sink == null) { return; }

        sink.tryEmitError(e);
        sinkMap.remove(requestId);
    }
}
