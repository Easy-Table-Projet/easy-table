package org.example.easytable.reservation.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ReservationGetByRestaurantReqDto {
    private Long restaurantId;
}
