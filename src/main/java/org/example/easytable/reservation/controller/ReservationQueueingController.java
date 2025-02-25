package org.example.easytable.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.queueing.ReservationServiceV2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v2/reservations")
@RequiredArgsConstructor
public class ReservationQueueingController {
    private final ReservationServiceV2 reservationService;

    @PostMapping("/{restaurantId}")
    public Mono<ReservationCreateResDto> createReservation(
            @PathVariable Long restaurantId,
            @RequestBody ReservationPostReqDto requestDto

    ) {
        Long memberId = AuthUtil.getId();

        return reservationService.submitReservation(new ReservationCreateReqDto(
                restaurantId, memberId, requestDto
        ));
    }

//    @GetMapping("/{restaurantId}")
//    public ResponseEntity<List<ReservationGetResDto>> getReservation(
//            @PathVariable("restaurantId") Long restaurantId
//    ) {
//        // 큐에 넣은 요청 결과를 받아오는 역할
//        // 재사용이 불가능한 객체이므로 메서드마다 별도로 생성
//        String requestId = UUID.randomUUID().toString();
//        CompletableFuture<List<ReservationGetResDto>> future = new CompletableFuture<>();
//        requestFutureStore.registerFuture(requestId, future);
//
//        if (!requestQueue.enqueue(
//            new ReservationGetByRestaurantReqDto(restaurantId, requestId))
//        ) {
//            throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
//        }
//
//        try {
//            return ResponseEntity.ok(future.get());
//        } catch (Exception e) {
//            throw new RuntimeException();
//        }
//    }
//
//
//    @GetMapping("/")
//    public ResponseEntity<List<ReservationGetResDto>> getReservationByMember() {
//        String requestId = UUID.randomUUID().toString();
//        CompletableFuture<List<ReservationGetResDto>> future = new CompletableFuture<>();
//        requestFutureStore.registerFuture(requestId, future);
//
//        if (!requestQueue.enqueue(new ReservationGetByMemberReqDto(requestId))) {
//            throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
//        }
//
//        try {
//            return ResponseEntity.ok(future.get());
//        } catch (Exception e) {
//            throw new RuntimeException();
//        }
//    }
//
//    @DeleteMapping("/{reservationId}")
//    public void deleteReservation(
//            @PathVariable("reservationId") Long reservationId
//    ) {
//        if (!requestQueue.enqueue(new ReservationDeleteReqDto(AuthUtil.getId()))) {
//            throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
//        }
//    }
}
