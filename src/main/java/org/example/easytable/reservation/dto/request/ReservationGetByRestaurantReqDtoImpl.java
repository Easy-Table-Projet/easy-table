package org.example.easytable.reservation.dto.request;

import lombok.Getter;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.RequestFutureStore;
import org.example.easytable.reservation.service.ReservationService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public class ReservationGetByRestaurantReqDtoImpl implements ReservationReqDto {
    private final Long restaurantId;
    private final String requestId;

    public ReservationGetByRestaurantReqDtoImpl(
            Long restaurantId,
            String requestId
    ) {
        this.restaurantId = restaurantId;
        this.requestId = requestId;
    }

    @Override
    public void process(ReservationService service, RequestFutureStore futureStore) {
        CompletableFuture<List<ReservationGetResDto>> future = futureStore.getFuture(requestId);
        System.out.println("saved RequestId: " + requestId);
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

    @Override
    public String toString() {
        return "ReservationGetByRestaurantReqDtoImpl{" +
            "restaurantId=" + restaurantId +
            ", requestId='" + requestId + '\'' +
            '}';
    }
}
