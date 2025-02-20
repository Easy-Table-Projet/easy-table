package org.example.easytable.config;

import org.example.easytable.reservation.dto.request.ReservationReqDto;
import org.example.easytable.reservation.service.queueing.RequestQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MockRequestQueue implements RequestQueue {
    private final BlockingQueue<ReservationReqDto> queue = new ArrayBlockingQueue<>(25);

    @Override
    public boolean enqueue(ReservationReqDto request) {
        queue.add(request);
        return true;
    }

    // 테스트에서 큐 프로세싱은 수행하지 않음
    @Override
    public void processQueue() {
        // no-op
    }

    public int getQueueSize() {
        return queue.size();
    }
}
