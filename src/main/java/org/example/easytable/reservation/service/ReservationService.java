package org.example.easytable.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.example.easytable.restaurant.service.RestaurantLockingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;
    private final RestaurantLockingService lockingService;

    //@RedissonLock(prefix = "restaurant:")
    @Transactional
    public ReservationCreateResDto createReservation(
            Long restaurantId, Long memberId, ReservationPostReqDto reservationPostReqDto
    ) throws CustomException {
        if (reservationPostReqDto.reservationTime().isBefore(LocalDateTime.now())) {
            throw CustomException.of(ErrorCode.BAD_REQUEST, "이미 기한이 지난 예약입니다");
        }

        Member reservingMember = memberRepository.findById(memberId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다"));

        Restaurant foundRestaurant = lockingService.atomicDecreaseRemainingTableCount(restaurantId);

        checkDuplicatedReservation(memberId, restaurantId, reservationPostReqDto);

        Reservation newReservation = Reservation.builder()
                .member(reservingMember)
                .restaurant(foundRestaurant)
                .reservationTime(reservationPostReqDto.reservationTime())
                .build();

        reservationRepository.save(newReservation);

        return ReservationCreateResDto.from(newReservation);
    }

    @Transactional
    public ReservationCreateResDto createReservation(ReservationCreateReqMessage dto) {
        return this.createReservation(dto.getRestaurantId(), dto.getMemberId(), dto.getReservationPostReqDto());
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

    public List<ReservationGetResDto> getReservationByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다");
        }

        // TODO: N+1 개선 필요 - Member, Restaurant 조회 시 발생
        return reservationRepository.findByMemberId(memberId).stream()
                .map(ReservationGetResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long memberId, Long reservationId) {

        if (!memberRepository.existsById(memberId)) {
            throw CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다"));

        if (!reservation.getMember().getId().equals(memberId)) {
            throw CustomException.of(ErrorCode.FORBIDDEN, "본인의 예약만 취소할 수 있습니다");
        }

        reservation.softDelete();
    }

    @Transactional
    public void bulkInsertReservations(int totalReservations) {
        Random random = new Random();
        List<Member> members = memberRepository.findAll();
        List<Restaurant> restaurants = restaurantRepository.findAll();

        List<Reservation> reservations = new ArrayList<>();

        for (int i = 0; i < totalReservations; i++) {
            // 랜덤 회원 및 식당 선택
            Member randomMember = members.get(random.nextInt(members.size()));
            Restaurant randomRestaurant = restaurants.get(random.nextInt(restaurants.size()));

            // 랜덤 예약 시간 (현재 시간 ±7일)
            LocalDateTime reservationTime = LocalDateTime.now()
                    .plusDays(random.nextInt(14) - 7)  // -7 ~ +7일 범위
                    .plusHours(random.nextInt(24))    // 0~23시간 랜덤 추가
                    .plusMinutes(random.nextInt(60)); // 0~59분 랜덤 추가

            // 랜덤 예약 상태
            ReservationStatus status = ReservationStatus.values()[random.nextInt(ReservationStatus.values().length)];

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .member(randomMember)
                    .restaurant(randomRestaurant)
                    .reservationTime(reservationTime)
                    .build();

            reservations.add(reservation);

            // 배치 처리 (1000건씩 저장 후 flush & clear)
            if (i % 1000 == 0 && i > 0) {
                reservationRepository.saveAll(reservations);
                reservationRepository.flush();
                reservations.clear();
                System.out.println("Inserted " + i + " reservations...");
            }
        }

        // 남은 데이터 저장
        if (!reservations.isEmpty()) {
            reservationRepository.saveAll(reservations);
            reservationRepository.flush();
        }

        System.out.println("✅ 10만 건의 예약 데이터 삽입 완료!");
    }

    private void checkDuplicatedReservation(
            Long memberId, Long restaurantId, ReservationPostReqDto reservationPostReqDto
    ) {
        List<Reservation> duplicatedReservations = reservationRepository.findDuplicatedReservations(
                memberId, restaurantId, reservationPostReqDto.reservationTime());

        if (!duplicatedReservations.isEmpty()) {
            throw CustomException.of(ErrorCode.BAD_REQUEST, "이미 해당 예약이 존재합니다.");
        }
    }
}
