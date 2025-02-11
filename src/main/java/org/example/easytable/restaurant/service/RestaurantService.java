package org.example.easytable.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;
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
@Transactional
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantResDto createRestaurant(RestaurantCreateDto dto) {
        Restaurant restaurant = Restaurant.newRestaurant(dto);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return RestaurantResDto.from(savedRestaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResDto findRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        //todo: 해당 아이디로 찾는 식당이 없는경우 예외처리 필요.
        return RestaurantResDto.from(restaurant);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResDto> findAllRestaurantByTitle(
            String restaurantName,
            Pageable pageable) { //todo: 주소 등 추가 검색 조건 파라미터 추가
        Page<Restaurant> restaurants = restaurantRepository.findAllRestaurantByTitle(restaurantName, pageable);
        //todo: 빈 리스트 일 경우 404 예외처리 필요.
        return restaurants.map(RestaurantResDto::from);
    }

    public RestaurantResDto updateRestaurantName(Long restaurantId, RestaurantNameUpdateReqDto dto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        //todo: 해당 아이디로 찾는 식당이 없는경우 예외처리 필요.
        restaurant.updateRestaurantName(dto.restaurantName());
        return RestaurantResDto.from(restaurant);
    }

    public void deleteRestaurantName(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).get();
        //todo: 해당 아이디로 찾는 식당이 없는경우 예외처리 필요.
        restaurant.deleteRestaurant();
    }
}
