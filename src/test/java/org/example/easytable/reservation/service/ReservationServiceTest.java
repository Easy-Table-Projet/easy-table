package org.example.easytable.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.dto.response.ReservationGetResDto;
import org.example.easytable.reservation.entity.Reservation;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.repository.ReservationRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.example.easytable.utils.AuthUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MemberRepository memberRepository;

    // AuthUtil의 정적 메서드를 Mocking하기 위한 객체
    // MockedStatic은 정적 메서드를 모의(Mock)하여 원하는 값을 반환하도록 설정하는 데 사용됨
    private MockedStatic<AuthUtil> authUtil;
    private static final Long MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        // AuthUtil 클래스를 Mocking하여 정적 메서드 getId()가 항상 1L을 반환하도록 설정
        authUtil = mockStatic(AuthUtil.class);
        authUtil.when(AuthUtil::getId).thenReturn(MEMBER_ID);
    }

    @AfterEach
    void tearDown() {
        // MockedStatic 인스턴스를 닫아 Mock을 해제 (테스트 간 간섭 방지)
        authUtil.close();
    }

    @Test
    @DisplayName("예약 등록 성공 테스트")
    void testCreateReservationSuccess() {
        // given (테스트 준비)
        // 예약 시간을 2024년 2월 14일 오전 10시로 설정
        LocalDateTime reservationTime = LocalDateTime.of(2024, 2, 14, 10, 0);

        // 예약 요청 DTO(데이터 전송 객체) 생성
        ReservationCreateReqDto reservationCreateReqDto = new ReservationCreateReqDto(reservationTime);

        // 회원(Member) 객체 생성 (빌더 패턴 사용)
        Member member = Member.builder()
                .name("John Doe")  // 회원 이름 설정
                .email("john@example.com")  // 회원 이메일 설정
                .build();

        Long restaurantId = 1L;
        // 레스토랑(Restaurant) 객체 생성
        Restaurant restaurant = Restaurant.builder()
                .name("Nice Restaurant")  // 레스토랑 이름 설정
                .address("123 Food Street")  // 주소 설정
                .restaurantCategory(RestaurantCategory.KOREAN)  // 레스토랑 카테고리 설정
                .build();

        // 예약(Reservation) 객체 생성
        Reservation reservation = Reservation.builder()
                .member(member)  // 예약한 회원 설정
                .restaurant(restaurant)  // 예약한 레스토랑 설정
                .reservationTime(reservationTime)  // 예약 시간 설정
                .build();


        // Mocking (가짜 객체 설정)
        // memberRepository에서 ID가 MEMBER_ID인 회원을 조회하면 위에서 만든 member 객체를 반환하도록 설정
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        // restaurantRepository에서 ID가 1인 레스토랑을 조회하면 위에서 만든 restaurant 객체를 반환하도록 설정
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // reservationRepository에서 예약을 저장할 때, 위에서 만든 reservation 객체를 반환하도록 설정
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when (실제 테스트 실행)
        // 예약 서비스를 호출하여 예약을 생성하고, 결과를 savedReservation에 저장
        ReservationCreateResDto savedReservation = reservationService.createReservation(restaurantId, reservationCreateReqDto);

        // then (검증 단계)
        // memberRepository.findById(1L) 메서드가 1번 호출되었는지 검증
        verify(memberRepository, times(1)).findById(MEMBER_ID);

        // restaurantRepository.findById(1L) 메서드가 1번 호출되었는지 검증
        verify(restaurantRepository, times(1)).findById(restaurantId);

        // reservationRepository.save() 메서드가 1번 호출되었는지 검증
        verify(reservationRepository, times(1)).save(any(Reservation.class));

        // 반환된 예약 정보가 예상한 예약 시간과 일치하는지 검증
        assertEquals(reservationTime, savedReservation.getReservationTime());

        // 반환된 예약 상태가 CONFIRMED인지 검증
        assertEquals(ReservationStatus.CONFIRMED, savedReservation.getStatus());
    }



    @Test
    @DisplayName("레스토랑 ID로 예약 목록 조회 성공 테스트")
    void testGetReservationByRestaurantSuccess() {
        // given
        LocalDateTime reservationTime = LocalDateTime.of(2024, 2, 14, 10, 0);

        Member member = Member.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .name("Nice Restaurant")
                .address("123 Food Street")
                .restaurantCategory(RestaurantCategory.KOREAN)
                .build();

        List<Reservation> reservations = Arrays.asList(
                new Reservation(member, restaurant, reservationTime),
                new Reservation(member, restaurant, reservationTime.plusHours(2))
        );

        // when
        when(reservationRepository.findByRestaurantId(1L)).thenReturn(reservations);

        List<ReservationGetResDto> result = reservationService.getReservationByRestaurant(1L);

        // then
        verify(reservationRepository, times(1)).findByRestaurantId(1L);

        assertEquals(2, result.size());
        assertEquals(reservationTime, result.get(0).getReservationTime());
        assertEquals(ReservationStatus.CONFIRMED, result.get(0).getStatus());
    }


}
