package org.example.easytable.reservation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.example.easytable.member.entity.Member;
import org.example.easytable.common.entity.UserType;
import org.example.easytable.reservation.dto.response.ReservationCreateRes;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    ReservationService reservationService;

    @Mock
    ReservationRepository reservationRepository;

    @Mock

    @Test
    public void save() throws Exception {
        //given
        Restaurant restaurant = new Restaurant(1L, "restaruant", "address", false, null);

        LocalDateTime reservationTime = LocalDateTime.now();

        Reservation reservation = Reservation.builder()
                .member(member)
                .restaurant(restaurant)
                .reservationTime(reservationTime)
                .status(ReservationStatus.CONFIRMED)
                .isDeleted(false)
                .build();
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        // when
        ReservationCreateRes savedReservation = reservationService.save(restaurant.getId(),
                reservationTime);

        // then
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        assertEquals(reservation.getMember().getId(), savedReservation.getMember_id());
        assertEquals(reservation.getRestaurant().getId(), savedReservation.getRestaurant_id());

    }

}