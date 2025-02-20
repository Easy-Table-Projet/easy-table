package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.request.ReservationReqDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Component
@Qualifier("collectionQueue")
@RequiredArgsConstructor
public class RequestCollectionQueueImpl implements RequestQueue {
    private final BlockingQueue<ReservationReqDto> queue;
    private final ReservationService service;
    private final RequestFutureStore requestFutureStore;

    @Override
    public boolean enqueue(ReservationReqDto request) {
        return queue.offer(request);
    }

    // 큐에 쌓인 요청들을 처리하는 메서드
    @Override
    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        List<ReservationReqDto> requests = new ArrayList<>();
        queue.drainTo(requests);

        for (ReservationReqDto req : requests) {
            req.process(service, requestFutureStore);
        }
    }
}
