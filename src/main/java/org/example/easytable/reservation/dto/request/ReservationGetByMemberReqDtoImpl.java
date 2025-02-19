package org.example.easytable.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.RequestFutureStore;
import org.example.easytable.reservation.service.ReservationService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationGetByMemberReqDtoImpl implements ReservationReqDto {
    private final String requestId;

    @JsonCreator
    public ReservationGetByMemberReqDtoImpl(@JsonProperty("requestId") String requestId) {
        this.requestId = requestId;
    }

    @Override
    public void process(ReservationService service, RequestFutureStore futureStore) {
        CompletableFuture<List<ReservationGetResDto>> future = futureStore.removeFuture(requestId);
        if (future == null) { throw new RuntimeException("CompletableFuture 조회 실패"); }

        List<ReservationGetResDto> result = service.getReservationByMember();
        try {
            future.complete(result);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }
}
