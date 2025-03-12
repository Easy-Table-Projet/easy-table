package org.example.easytable.reservation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.queueing.ReservationServiceV3;
import org.example.easytable.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationQueueingControllerV3 {
    private final ReservationServiceV3 reservationService;

    @PostMapping("/{restaurantId}")
    public ResponseEntity<ReservationCreateResDto> createReservation(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("restaurantId") Long restaurantId,
            @RequestBody ReservationPostReqDto requestDto

    ) throws Exception {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        //---------------------------------------------------------------------------------------

        Long memberId = userDetails.getId();

        ReservationCreateResDto result = reservationService.queueRequest(new ReservationCreateReqDto(
                restaurantId, memberId, requestDto
        ));

        //---------------------------------------------------------------------------------------

        stopWatch.stop();
        log.info("execution time: {}(ms)", stopWatch.getTotalTimeMillis());

        //---------------------------------------------------------------------------------------

        return ResponseEntity.ok(result);
    }
}
