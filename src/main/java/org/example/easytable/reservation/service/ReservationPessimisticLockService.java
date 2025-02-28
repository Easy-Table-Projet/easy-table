package org.example.easytable.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.aop.annotation.LockKey;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationPessimisticLockService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReservationCreateResDto createReservation(@LockKey Long restaurantId, Long memberId, ReservationPostReqDto reservationPostReqDto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다"));

        Restaurant restaurant = restaurantRepository.findFirstById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        restaurant.decreaseRemainingTableCount();

        Reservation newReservation = Reservation.builder()
                .member(member)
                .restaurant(restaurant)
                .reservationTime(reservationPostReqDto.reservationTime())
                .build();

        reservationRepository.save(newReservation);

        return ReservationCreateResDto.from(newReservation);
    }
}
