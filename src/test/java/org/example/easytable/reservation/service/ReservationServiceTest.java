package org.example.easytable.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.example.easytable.member.entity.Member;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Test
    public void 예약_등록_성공() throws Exception {
        // given
        Long restaurantId = 1L;
        LocalDateTime reservationTime = LocalDateTime.now();
        int orderCount = 1;
        int validSeatCount = 20;
        Restaurant restaurant
                = new Restaurant(restaurantId, "restaurant", "address", validSeatCount, false, RestaurantCategory.KOREAN ,null);

        Reservation reservation = Reservation.builder()
                .member(null)
                .restaurant(restaurant)
                .reservationTime(reservationTime)
                .status(ReservationStatus.CONFIRMED)
                .isDeleted(false)
                .build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when
        ReservationCreateResDto savedReservation = reservationService.save(restaurantId,
                reservationTime, orderCount);

        // then
        verify(restaurantRepository, times(1)).findById(restaurantId);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        assertEquals(reservation.getRestaurant().getId(), savedReservation.getRestaurantId());
    }

    @Test
    public void 식당에_등록된_예약_조회() throws Exception {

        // given
        Member member = Member.builder()
                .id(1L)
                .email("teste@naver.com")
                .name("name")
                .password("password")
                .address("address")
                .isDeleted(false)
                .reservations(null)
                .build();

        Long restaurantId = 1L;
        LocalDateTime reservationTime = LocalDateTime.now();

        Restaurant restaurant = Restaurant.builder()
                .id(restaurantId)
                .name("name")
                .address("주소")
                .isDeleted(false)
                .reservations(null).build();

        Reservation reservation = Reservation.builder()
                .member(member)
                .restaurant(restaurant)
                .reservationTime(reservationTime)
                .status(ReservationStatus.CONFIRMED)
                .isDeleted(false)
                .build();

        when(reservationRepository.findByRestaurantId(restaurantId)).thenReturn(Arrays.asList(reservation));
        // when
        List<ReservationGetResDto> reservationList = reservationService.getReservationByRestaurant(restaurantId);
        // then
        assertEquals(1,reservationList.size());
        assertEquals(member.getId(), reservationList.get(0).getMemberId());
        assertEquals(restaurant.getId(), reservationList.get(0).getRestaurantId());
        assertEquals(reservationTime, reservationList.get(0).getReservationTime());
        assertEquals(ReservationStatus.CONFIRMED, reservationList.get(0).getStatus());
    }

    @Test
    public void 예약_삭제_성공() throws Exception {
        // given
        Long restaurantId = 1L;
        Long reservationId = 1L; // 테스트용 예약 ID

        Restaurant restaurant = Restaurant.builder()
                .id(restaurantId)
                .name("name")
                .address("주소")
                .isDeleted(false)
                .reservations(null).build();

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .restaurant(restaurant)
                .status(ReservationStatus.CONFIRMED)
                .isDeleted(false)
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // when
        reservationService.deleteReservation(restaurantId, reservationId);

        // then
        verify(reservationRepository, times(1)).delete(reservation);
    }
}
