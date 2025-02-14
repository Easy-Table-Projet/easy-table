package org.example.easytable.restaurant.service;

import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;
import org.example.easytable.restaurant.dto.request.RestaurantNameUpdateReqDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @InjectMocks
    RestaurantService restaurantService;
    @Mock
    RestaurantRepository restaurantRepository;

    private RestaurantCreateDto restaurantCreateDto;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurantCreateDto = new RestaurantCreateDto("가게이름", "가게주소",10, "KOREAN");
        restaurant = Restaurant.builder()
                .name(restaurantCreateDto.name())
                .address(restaurantCreateDto.address())
                .validSeatCount(restaurantCreateDto.validSeatCount())
                .restaurantCategory(RestaurantCategory.valueOf(restaurantCreateDto.category()))
                .build();
        ReflectionTestUtils.setField(restaurant, "id", 1L);
    }

    @Test
    void 가게_저장_성공_테스트() {
        //given
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        //when
        RestaurantResDto result = restaurantService.createRestaurant(restaurantCreateDto);
        //then
        assertThat(result.name()).isEqualTo("가게이름");
        assertThat(result.address()).isEqualTo("가게주소");
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void 가게_단건_조회_테스트() {
        // given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // when
        RestaurantResDto result = restaurantService.findRestaurantById(1L);

        // then
        assertThat(result.name()).isEqualTo("가게이름");
        assertThat(result.address()).isEqualTo("가게주소");
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void 가게_단건_조회_예외처리_테스트() {
        // given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> restaurantService.findRestaurantById(1L));
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void 가게_다건_조회_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Restaurant> restaurantPage = new PageImpl<>(List.of(restaurant), pageable, 1);

        RestaurantCategory category = RestaurantCategory.valueOf("KOREAN");

        when(restaurantRepository.findAllRestaurantByTitleAndCategory("가게이름", category, pageable))
                .thenReturn(restaurantPage);

        // when
        Page<RestaurantResDto> result = restaurantService.findAllRestaurantByTitleAndCategory("가게이름", "KOREAN", pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(restaurantRepository, times(1)).findAllRestaurantByTitleAndCategory("가게이름", category, pageable);
    }

    @Test
    void 가게_정보_수정_성공() {
        // given
        RestaurantNameUpdateReqDto dto = new RestaurantNameUpdateReqDto("수정수정");
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // when
        RestaurantResDto result = restaurantService.updateRestaurantName(1L, dto);

        // then
        assertThat(result.name()).isEqualTo("수정수정");
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void 가게_정보_수정_예외처리_테스트() {
        // given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());
        RestaurantNameUpdateReqDto dto = new RestaurantNameUpdateReqDto("수정수정");

        // when & then
        assertThrows(RuntimeException.class, () -> restaurantService.updateRestaurantName(1L, dto));
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void deleteRestaurantName_성공() {
        // given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // when
        restaurantService.deleteRestaurantName(1L);

        // then
        assertThat(restaurant.isDeleted()).isTrue();
        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void deleteRestaurantName_예외처리_테스트() {
        // given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> restaurantService.deleteRestaurantName(1L));
        verify(restaurantRepository, times(1)).findById(1L);
    }

}
