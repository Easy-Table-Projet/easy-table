package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.service.ReservationService;

public interface ReservationReqDto<T> {
    void process(ReservationService service);
}
