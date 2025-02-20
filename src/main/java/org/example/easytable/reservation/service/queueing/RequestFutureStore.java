package org.example.easytable.reservation.service.queueing;

import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestFutureStore {
    // Reservation 외의 다른 곳에서도 사용할 경우 제네릭 타입 수정할 것
    private final ConcurrentHashMap<
            String, CompletableFuture<List<ReservationGetResDto>>> futureMap = new ConcurrentHashMap<>();

    // @Value("${queue-capacity:25}")
    private final int capacity = 25;

    public void registerFuture(String requestId, CompletableFuture<List<ReservationGetResDto>> future) {
        if (capacity <= futureMap.size()) { throw new RuntimeException("RequestFutureStore 용량 초과"); }
        futureMap.put(requestId, future);
    }

    public CompletableFuture<List<ReservationGetResDto>> getFuture(String requestId) {
        return futureMap.get(requestId);
    }

    public CompletableFuture<List<ReservationGetResDto>> removeFuture(String requestId) {
        return futureMap.remove(requestId);
    }
}
