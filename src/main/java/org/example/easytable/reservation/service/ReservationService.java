package org.example.easytable.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.aop.annotation.LockKey;
import org.example.easytable.common.aop.annotation.RedissonLock;
import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    @RedissonLock(prefix = "restaurant:")
    @Transactional
    public ReservationCreateResDto createReservation(@LockKey Long restaurantId, Long memberId, ReservationPostReqDto reservationCreateReqDto) {

        System.out.println("Creating reservation with memberId: " + memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        restaurant.decreaseRemainingTableCount();

        Reservation newReservation = Reservation.builder()
                .member(member)
                .restaurant(restaurant)
                .reservationTime(reservationCreateReqDto.reservationTime())
                .build();

        reservationRepository.save(newReservation);

        return ReservationCreateResDto.from(newReservation);
    }


    public List<ReservationGetResDto> getReservationByRestaurant(Long restaurantId) {

        if (!restaurantRepository.existsById(restaurantId)) {
            throw CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다");
        }

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

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다"));

        if (!reservation.getMember().getId().equals(memberId)) {
            throw CustomException.of(ErrorCode.FORBIDDEN, "본인의 예약만 취소할 수 있습니다");
        }

        reservation.softDelete();
    }

}
