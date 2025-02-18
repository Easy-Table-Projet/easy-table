package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationService;

public record ReservationCreateReqDto (
        Long restaurantId,
        Long memberId,
        ReservationPostReqDto reservationPostReqDto
) implements ReservationReqDto<ReservationCreateResDto> {

    @Override
    public void process(ReservationService service) {
        service.createReservation(restaurantId, memberId, reservationPostReqDto);
    }
}
