package org.example.easytable.reservation.repository;

import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;

import java.util.concurrent.TimeoutException;

public interface MessagePublisher {
    public void publish(ReservationCreateReqDto dto) throws TimeoutException;
}
