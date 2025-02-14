package org.example.easytable.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
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
    public ReservationCreateResDto save(Long restaurantId, LocalDateTime reservationTime) {

        Restaurant foundRestaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));

        Long memberId = AuthUtil.getId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND));

        Reservation createdReservation = Reservation.builder()
                .member(member)
                .restaurant(foundRestaurant)
                .reservationTime(reservationTime)
                .status(ReservationStatus.CONFIRMED)
                .isDeleted(false)
                .build();

        reservationRepository.save(createdReservation);

        return ReservationCreateResDto.from(createdReservation);
    }


    public List<ReservationGetResDto> getReservationByRestaurant(Long restaurantId) {

        List<Reservation> reservationList = reservationRepository.findByRestaurantId(restaurantId);

        return reservationList.stream().map(ReservationGetResDto::from)
                .collect(Collectors.toList());
    }

    public List<ReservationGetResDto> getReservationByMember(Long memberId) {

        List<Reservation> reservationList = reservationRepository.findByMemberId(memberId);

        return reservationList.stream().map(ReservationGetResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long restaurantId, Long reservationId) {
        Reservation foundReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다"));

        if (!foundReservation.getRestaurant().getId().equals(restaurantId)) {
            throw CustomException.of(ErrorCode.BAD_REQUEST, "이 예약은 해당 식당에 속하지 않습니다.");
        }
        foundReservation.deleteReservation();
    }

}
