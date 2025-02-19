package org.example.easytable.reservation.controller;

import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.reservation.dto.request.*;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.RequestFutureStore;
import org.example.easytable.reservation.service.RequestQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v2/reservations")
public class ReservationQueueingController {
    private final RequestQueue requestQueue;
    private final RequestFutureStore requestFutureStore;

    // 사용할 queue 종류에 따라 Qualifier 값 변경할 것
    public ReservationQueueingController(
            @Qualifier("collectionQueue") RequestQueue requestQueue,
            RequestFutureStore requestFutureStore
    ) {
        this.requestQueue = requestQueue;
        this.requestFutureStore = requestFutureStore;
    }

    @PostMapping("/{restaurantId}")
    public ResponseEntity<ReservationCreateResDto> createReservation(
            @PathVariable Long restaurantId,
            @RequestBody ReservationPostReqDto requestDto
    ) {
        Long memberId = AuthUtil.getId();

        if (!requestQueue.enqueue(
            new ReservationCreateReqDtoImpl(restaurantId, memberId, requestDto))
        ) {
            throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<ReservationGetResDto>> getReservation(
            @PathVariable("restaurantId") Long restaurantId
    ) {
        // 큐에 넣은 요청 결과를 받아오는 역할
        // 재사용이 불가능한 객체이므로 메서드마다 별도로 생성
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<List<ReservationGetResDto>> future = new CompletableFuture<>();
        requestFutureStore.registerFuture(requestId, future);

        if (!requestQueue.enqueue(
            new ReservationGetByRestaurantReqDtoImpl(restaurantId, requestId))
        ) {
            throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
        }

        try {
            return ResponseEntity.ok(future.get());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    
    @GetMapping("/")
    public ResponseEntity<List<ReservationGetResDto>> getReservationByMember() {
        // TODO: UUID 생성 부분을 유틸로 분리할 것
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<List<ReservationGetResDto>> future = new CompletableFuture<>();
        requestFutureStore.registerFuture(requestId, future);

        if (!requestQueue.enqueue(new ReservationGetByMemberReqDtoImpl(requestId))) {
            throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
        }

        try {
            return ResponseEntity.ok(future.get());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @DeleteMapping("/{reservationId}")
    public void deleteReservation(
            @PathVariable("reservationId") Long reservationId
    ) {
        if (!requestQueue.enqueue(new ReservationDeleteReqDtoImpl(AuthUtil.getId()))) {
            throw CustomException.of(ErrorCode.TOO_MANY_REQUESTS);
        }
    }
}
