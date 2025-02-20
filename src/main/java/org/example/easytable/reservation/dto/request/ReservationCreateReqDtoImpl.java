package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.service.queueing.RequestFutureStore;
import org.example.easytable.reservation.service.ReservationService;

public record ReservationCreateReqDtoImpl(
        Long restaurantId,
        Long memberId,
        ReservationPostReqDto reservationPostReqDto
) implements ReservationReqDto {

    @Override
    public void process(ReservationService service, RequestFutureStore futureStore) {
        service.createReservation(restaurantId, memberId, reservationPostReqDto);
    }
}
