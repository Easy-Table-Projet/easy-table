//package org.example.easytable.reservation.service;
//
//import jakarta.persistence.EntityManager;
//import lombok.RequiredArgsConstructor;
//import org.example.easytable.common.aop.annotation.RedissonLock;
//import org.example.easytable.exception.CustomException;
//import org.example.easytable.exception.ErrorCode;
//import org.example.easytable.member.entity.Member;
//import org.example.easytable.member.repository.MemberRepository;
//import org.example.easytable.reservation.entity.Reservation;
//import org.example.easytable.reservation.repository.ReservationRepository;
//import org.example.easytable.restaurant.entity.Restaurant;
//import org.example.easytable.restaurant.repository.RestaurantRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//// ReservationService의 AOP Self-invocation 문제 해결을 위한 클래스
//@Service
//@RequiredArgsConstructor
//public class ReservationLockingService {
//    private final EntityManager entityManager;
//    private final MemberRepository memberRepository;
//    private final ReservationRepository reservationRepository;
//    private final RestaurantRepository restaurantRepository;
//
//    @RedissonLock(key = "'lock:restaurant:' + #p0")
//    @Transactional
//    public void saveReservationWithLock(Long restaurantId, Long memberId, Reservation reservation, int guestCount) {
//        Member foundMember = memberRepository.findById(memberId.intValue())
//                .orElseThrow(() -> new RuntimeException("유저 조회 실패"));
//        Restaurant foundRestaurant = restaurantRepository.findById(restaurantId)
//                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 식당입니다"));
//
//        if (foundRestaurant.isReservationAvailable(guestCount)) {
//            reservation.setRestaurant(foundRestaurant);
//            reservation.setMember(foundMember);
//            reservationRepository.save(reservation);
//
//            foundRestaurant.changeValidSeatCount(-guestCount);
//            restaurantRepository.save(foundRestaurant);
//        }
//
//        entityManager.flush();
//    }
//
//}
