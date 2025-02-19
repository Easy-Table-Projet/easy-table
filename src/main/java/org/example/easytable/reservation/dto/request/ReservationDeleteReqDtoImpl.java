package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.service.RequestFutureStore;
import org.example.easytable.reservation.service.ReservationService;

public record ReservationDeleteReqDtoImpl(
        Long reservationId
) implements ReservationReqDto {

    @Override
    public void process(ReservationService service, RequestFutureStore futureStore) {
        service.deleteReservation(reservationId);
    }
}
