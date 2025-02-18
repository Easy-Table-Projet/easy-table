package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.service.ReservationService;

public record ReservationDeleteReqDto(
        Long reservationId
) implements ReservationReqDto<Boolean> {

    @Override
    public void process(ReservationService service) {
        service.deleteReservation(reservationId);
    }
}
