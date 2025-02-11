package org.example.easytable.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.dto.request.CreateRestaurantDto;
import org.example.easytable.restaurant.dto.request.RestaurantNameUpdateReqDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public RestaurantResDto findRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        //todo: 해당 아이디로 찾는 식당이 없는경우 예외처리 필요.
        return RestaurantResDto.from(restaurant);
    }

    public Page<RestaurantResDto> findAllRestaurantByTitle(
            String restaurantName,
            Pageable pageable) { //todo: 주소 등 추가 검색 조건 파라미터 추가
        Page<Restaurant> restaurants = restaurantRepository.findAllRestaurantByTitle(restaurantName,pageable);
        //todo: 빈 리스트 일 경우 404 예외처리 필요.
        return restaurants.map(RestaurantResDto::from);
    }

    @Transactional
    public RestaurantResDto updateRestaurantName(Long restaurantId, RestaurantNameUpdateReqDto dto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        //todo: 해당 아이디로 찾는 식당이 없는경우 예외처리 필요.
        restaurant.updateRestaurantName(dto.restaurantName());
        return  RestaurantResDto.from(restaurant);
    }
}
