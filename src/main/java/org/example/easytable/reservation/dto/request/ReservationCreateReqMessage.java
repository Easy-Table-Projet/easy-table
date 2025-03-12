package org.example.easytable.reservation.dto.request;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ReservationCreateReqMessage {
    private final String requestId = UUID.randomUUID().toString();
    private Long restaurantId;
    private Long memberId;
    private ReservationPostReqDto reservationPostReqDto;
}
