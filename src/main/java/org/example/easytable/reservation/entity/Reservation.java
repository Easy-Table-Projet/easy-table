package org.example.easytable.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.easytable.common.entity.BaseEntity;
import org.example.easytable.member.entity.Member;
import org.example.easytable.restaurant.entity.Restaurant;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Setter
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @Setter
    private Restaurant restaurant;

    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private int guestCount;

    private boolean isDeleted;
}


