package org.example.easytable.reservation.service.queueing;

import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;

public interface RequestQueue {
    public boolean enqueue(ReservationCreateReqDto request);
    public void processQueue();
}
