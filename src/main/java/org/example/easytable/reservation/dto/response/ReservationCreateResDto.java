package org.example.easytable.reservation.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;

@RequiredArgsConstructor(staticName = "from")
@Getter
public class ReservationCreateResDto {

    private final Long reservationId;
    private final Long memberId;
    private final Long restaurantId;
    private final LocalDateTime reservationTime;
    private final ReservationStatus status;

    public static ReservationCreateResDto from(Reservation reservation) {
        return from(
                reservation.getId(),
                reservation.getMember().getId(),
                reservation.getRestaurant().getId(),
                reservation.getReservationTime(),
                reservation.getStatus()
        );
    }
}
