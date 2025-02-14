package org.example.easytable.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.common.aop.annotation.RedissonLock;
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
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReservationCreateResDto save(Long restaurantId, LocalDateTime reservationTime, int guestCount, Long memberId) {
        Member member = memberRepository.findById(memberId.intValue())
                .orElseThrow(() -> new RuntimeException("유저 조회 실패"));

        Reservation createdReservation = Reservation.builder()
                .member(member) //member 코드 추가시에 member 지정 가능
                .reservationTime(reservationTime)
                .status(ReservationStatus.CONFIRMED)
                .guestCount(guestCount)
                .isDeleted(false)
                .build();

        saveReservationWithLock(restaurantId, createdReservation, guestCount);

        return new ReservationCreateResDto(
                createdReservation.getId(), createdReservation.getMember().getId(),
                createdReservation.getRestaurant().getId(), createdReservation.getReservationTime(),
                createdReservation.getStatus());
    }


    public List<ReservationGetResDto> getReservationByRestaurant(Long restaurantId) {

        List<Reservation> reservationList = reservationRepository.findByRestaurantId(restaurantId);

        return reservationList.stream().map(reservation -> new ReservationGetResDto(
                        reservation.getMember().getId(),
                        reservation.getRestaurant().getId(),
                        reservation.getReservationTime(),
                        reservation.getStatus()))
                .collect(Collectors.toList());
    }

    public List<ReservationGetResDto> getReservationByMember(Long memberId) {

        List<Reservation> reservationList = reservationRepository.findByMemberId(memberId);

        return reservationList.stream().map(reservation -> new ReservationGetResDto(
                reservation.getMember().getId(),
                reservation.getRestaurant().getId(),
                reservation.getReservationTime(),
                reservation.getStatus())).collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long restaurantId, Long reservationId) {
        Reservation foundReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다"));

        if (!foundReservation.getRestaurant().getId().equals(restaurantId)) {
            throw CustomException.of(ErrorCode.BAD_REQUEST, "이 예약은 해당 식당에 속하지 않습니다.");
        }

        deleteReservationWithLock(restaurantId, foundReservation);
    }


    public Restaurant findRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));
    }


    @RedissonLock(key = "'lock:restaurant:' + #restaurantId")
    @Transactional
    public void saveReservationWithLock(Long restaurantId, Reservation reservation, int guestCount) {
        Restaurant foundRestaurant = findRestaurantById(restaurantId);
        reservation.setRestaurant(foundRestaurant);
        reservationRepository.save(reservation);

        foundRestaurant.changeValidSeatCount(-guestCount);
        restaurantRepository.save(foundRestaurant);
    }

    @RedissonLock(key = "'lock:restaurant:' + #restaurantId")
    @Transactional
    public void deleteReservationWithLock(Long restaurantId, Reservation reservation) {
        Restaurant foundRestaurant = findRestaurantById(restaurantId);
        reservationRepository.delete(reservation);
        foundRestaurant.changeValidSeatCount(reservation.getGuestCount());
        restaurantRepository.save(foundRestaurant);
    }
}
