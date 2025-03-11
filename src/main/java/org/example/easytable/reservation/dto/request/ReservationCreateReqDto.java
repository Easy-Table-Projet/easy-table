package org.example.easytable.reservation.dto.request;

import lombok.*;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ReservationCreateReqDto {
    private final Long requestId = System.currentTimeMillis();
    private Long restaurantId;
    private Long memberId;
    private ReservationPostReqDto reservationPostReqDto;
}
