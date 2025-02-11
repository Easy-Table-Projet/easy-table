package org.example.easytable.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.dto.request.CreateRestaurantDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    @Transactional
    public RestaurantResDto createRestaurant(CreateRestaurantDto dto) {
        Restaurant restaurant = Restaurant.newRestaurant(dto);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return RestaurantResDto.from(savedRestaurant);
    }
}
