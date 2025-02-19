package org.example.easytable.reservation.service;

import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestFutureStore {
    // Reservation 외의 다른 곳에서도 사용할 경우 제네릭 타입 수정할 것
    private final ConcurrentHashMap<
            String, CompletableFuture<List<ReservationGetResDto>>> futureMap = new ConcurrentHashMap<>();

    public void registerFuture(String requestId, CompletableFuture<List<ReservationGetResDto>> future) {
        futureMap.put(requestId, future);
    }

    public CompletableFuture<List<ReservationGetResDto>> getFuture(String requestId) {
        return futureMap.get(requestId);
    }

    public CompletableFuture<List<ReservationGetResDto>> removeFuture(String requestId) {
        return futureMap.remove(requestId);
    }
}
