package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.service.RequestFutureStore;
import org.example.easytable.reservation.service.ReservationService;

public interface ReservationReqDto {
    void process(ReservationService service, RequestFutureStore futureStore);
}
