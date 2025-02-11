package org.example.easytable.restaurant.service;

import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
        restaurantCreateDto = new RestaurantCreateDto("가게이름", "가게주소");
        restaurant = Restaurant.newRestaurant(restaurantCreateDto);
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

}
