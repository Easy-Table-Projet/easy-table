package org.example.easytable.reservation.dto.request;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReservationCreateReqDto {

    private final LocalDateTime reservationTime;
    private final int guestCount;

}
