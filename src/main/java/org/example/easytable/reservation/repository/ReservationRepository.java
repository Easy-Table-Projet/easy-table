package org.example.easytable.reservation.repository;

import java.util.List;
import org.example.easytable.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.member.id = :id")
    List<Reservation> findByMemberId(@Param("id") Long id);
}
