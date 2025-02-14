package org.example.easytable.reservation.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.example.easytable.utils.AuthUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReservationCreateResDto createReservation(Long restaurantId, ReservationCreateReqDto reservationCreateReqDto) {

        Long memberId = AuthUtil.getId();

        Member foundMember = memberRepository.findById(memberId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다"));

        Restaurant foundRestaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        Reservation newReservation = Reservation.builder()
                .member(foundMember)
                .restaurant(foundRestaurant)
                .reservationTime(reservationCreateReqDto.reservationTime())
                .build();

        reservationRepository.save(newReservation);

        return ReservationCreateResDto.from(newReservation);
    }


    public List<ReservationGetResDto> getReservationByRestaurant(Long restaurantId) {
//        if (reservationList.isEmpty()) {
//            throw CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다");
//        }
//
        List<Reservation> reservationList = reservationRepository.findByRestaurantId(restaurantId);

        // TODO: N+1 개선 필요 - Member, Restaurant 조회 시 발생
        return reservationList.stream()
                .map(ReservationGetResDto::from)
                .collect(Collectors.toList());
    }

    public List<ReservationGetResDto> getReservationByMember() {
        Long memberId = AuthUtil.getId();

        // TODO: N+1 개선 필요 - Member, Restaurant 조회 시 발생
        return reservationRepository.findByMemberId(memberId).stream()
                .map(ReservationGetResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long reservationId) {
        Long memberId = AuthUtil.getId();

        Reservation foundReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다"));

        if (!foundReservation.getMember().getId().equals(memberId)) {
            throw CustomException.of(ErrorCode.FORBIDDEN, "본인의 예약만 취소할 수 있습니다");
        }

        foundReservation.softDelete();
    }

}
