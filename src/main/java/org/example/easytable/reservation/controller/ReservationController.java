package org.example.easytable.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.example.easytable.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<ReservationCreateResDto> createReservation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long restaurantId,
            @RequestBody ReservationPostReqDto reservationPostDto
    ) {
        Long memberId = userDetails.getId();

        reservationService.createReservation(restaurantId, memberId, reservationPostDto);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<ReservationGetResDto>> getReservation(
            @PathVariable("restaurantId") Long restaurantId
    ) {

        List<ReservationGetResDto> reservation = reservationService.getReservationByRestaurant(
                restaurantId);

        return new ResponseEntity<>(reservation, HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<List<ReservationGetResDto>> getReservationByMember(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long memberId = userDetails.getId();
        List<ReservationGetResDto> reservations = reservationService.getReservationByMember(memberId);
        return ResponseEntity.ok(reservations);
    }

    // 🔹 현재 로그인한 회원의 예약 삭제
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long reservationId
    ) {
        Long memberId = userDetails.getId();
        reservationService.deleteReservation(memberId, reservationId);
        return ResponseEntity.noContent().build(); // HTTP 204 응답 (성공, 내용 없음)
    }
  
    @PostMapping("/dummy")
    public void deleteReservation(){
        reservationService.bulkInsertReservations(100000);
    }
}
