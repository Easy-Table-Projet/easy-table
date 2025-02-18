package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.ReservationService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record ReservationGetByRestaurantReqDto(
        Long restaurantId,
        CompletableFuture<List<ReservationGetResDto>> future
) implements ReservationReqDto<List<ReservationGetResDto>> {

    @Override
    public void process(ReservationService service) {
        try {
            future.complete(service.getReservationByRestaurant(restaurantId));
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }
}
