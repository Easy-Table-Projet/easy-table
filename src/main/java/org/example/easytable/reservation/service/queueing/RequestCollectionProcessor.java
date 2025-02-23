package org.example.easytable.reservation.service.queueing;

import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RequestCollectionProcessor {
    private final RequestFutureStore futureStore;
    private final RequestQueue requestQueue;
    // 동기화를 위한 공유 락 객체
    private final Object lock = new Object();

    public RequestCollectionProcessor(
        RequestFutureStore futureStore,
        @Qualifier("collectionQueue") RequestQueue requestQueue
    ) {
        this.futureStore = futureStore;
        this.requestQueue = requestQueue;
    }

    public void registerAndEnqueue(
        String requestId, ReservationCreateReqDto request, CompletableFuture<List<ReservationGetResDto>> future
    ) {
        synchronized (lock) {
            futureStore.registerFuture(requestId, future);
            boolean enqueued = requestQueue.enqueue(request);
            if (!enqueued) {
                futureStore.removeFuture(requestId);
                throw new RuntimeException("Failed to enqueue request");
            }
        }
    }
}
