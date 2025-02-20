package org.example.easytable.reservation.dto.request;

import org.example.easytable.reservation.service.queueing.RequestFutureStore;
import org.example.easytable.reservation.service.ReservationService;

//@JsonTypeInfo(
//        use = JsonTypeInfo.Id.NAME,
//        include = JsonTypeInfo.As.PROPERTY,
//        property = "type"
//)
//@JsonSubTypes({
//    @JsonSubTypes.Type(value = ReservationGetByRestaurantReqDtoImpl.class, name = "restaurant"),
//    @JsonSubTypes.Type(value = ReservationGetByMemberReqDtoImpl.class, name = "member"),
//    @JsonSubTypes.Type(value = ReservationCreateReqDtoImpl.class, name = "create"),
//    @JsonSubTypes.Type(value = ReservationDeleteReqDtoImpl.class, name = "delete")
//})
public interface ReservationReqDto {
    void process(ReservationService service, RequestFutureStore futureStore);
}
