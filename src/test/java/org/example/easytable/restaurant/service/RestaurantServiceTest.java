package org.example.easytable.restaurant.service;

import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @InjectMocks
    RestaurantService restaurantService;
    @Mock
    RestaurantRepository restaurantRepository;

    @Test
    void 가게_저장_성공_테스트(){
        //given
        RestaurantCreateDto restaurantCreateDto = new RestaurantCreateDto("가게이름", "가게주소");
        Restaurant restaurant = Restaurant.newRestaurant(restaurantCreateDto);
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        //when
        RestaurantResDto result = restaurantService.createRestaurant(restaurantCreateDto);

        //then
        assertThat(result.name()).isEqualTo("가게이름");
        assertThat(result.address()).isEqualTo("가게주소");
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }
}
