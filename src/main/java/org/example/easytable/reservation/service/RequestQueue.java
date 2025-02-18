package org.example.easytable.reservation.service;

import org.example.easytable.reservation.dto.request.ReservationReqDto;

public interface RequestQueue {
    public boolean enqueue(ReservationReqDto<?> request);
    public void processQueue();
}
