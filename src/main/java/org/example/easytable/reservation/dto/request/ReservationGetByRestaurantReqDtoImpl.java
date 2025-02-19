package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.RequestFutureStore;
import org.example.easytable.reservation.service.ReservationService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record ReservationGetByRestaurantReqDtoImpl(
        Long restaurantId,
        String requestId
) implements ReservationReqDto {

    @Override
    public void process(ReservationService service, RequestFutureStore futureStore) {
        CompletableFuture<List<ReservationGetResDto>> future = futureStore.getFuture(requestId);
        if (future == null) { throw new RuntimeException("CompletableFuture 조회 실패"); }

        List<ReservationGetResDto> result = service.getReservationByRestaurant(restaurantId);
        try {
            future.complete(result);
        } catch (Exception e) {
            future.completeExceptionally(e);
        } finally {
            futureStore.removeFuture(requestId);
        }
    }
}
