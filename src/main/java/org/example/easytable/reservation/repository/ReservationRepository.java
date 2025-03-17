package org.example.easytable.reservation.repository;

import org.example.easytable.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("SELECT r FROM Reservation r WHERE r.member.id = :id")
    List<Reservation> findByMemberId(@Param("id") Long id);

    @Query("SELECT r FROM Reservation r WHERE r.restaurant.id = :id")
    List<Reservation> findByRestaurantId(@Param("id") Long id);

    @Query("SELECT r " +
            "FROM Reservation r " +
            "WHERE r.member.id = :memberId AND r.restaurant.id = :restaurantId AND r.reservationTime = :reservationTime")
    List<Reservation> findDuplicatedReservations(
            @Param("memberId") Long memberId,
            @Param("restaurantId") Long restaurantId,
            @Param("reservationTime") LocalDateTime reservationTime);
}
