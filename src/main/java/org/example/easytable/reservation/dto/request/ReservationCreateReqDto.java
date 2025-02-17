package org.example.easytable.reservation.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ReservationCreateReqDto {

    private final LocalDateTime reservationTime;
    private final int guestCount;

}
