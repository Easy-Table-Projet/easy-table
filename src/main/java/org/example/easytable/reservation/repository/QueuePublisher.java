package org.example.easytable.reservation.repository;

import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;

public interface QueuePublisher {
    public void publish(ReservationCreateReqDto dto);
}
