package org.example.easytable.reservation.dto.request;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ReservationCreateReqDto {
    private final String requestId = UUID.randomUUID().toString();
    private Long restaurantId;
    private Long memberId;
    private ReservationPostReqDto reservationPostReqDto;
}
