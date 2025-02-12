package org.example.easytable.reservation.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.entity.ReservationStatus;

@RequiredArgsConstructor
@Getter
public class ReservationGetResDto {

    private final Long memberId;
    private final Long restaurantId;
    private final LocalDateTime reservationTime;
    private final ReservationStatus status;


}

