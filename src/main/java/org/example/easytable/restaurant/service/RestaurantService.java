package org.example.easytable.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;
import org.example.easytable.restaurant.dto.request.RestaurantNameUpdateReqDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantResDto createRestaurant(RestaurantCreateDto dto) {
        Restaurant restaurant = Restaurant.builder()
                .name(dto.name())
                .address(dto.address())
                .validSeatCount(dto.validSeatCount())
                .restaurantCategory(RestaurantCategory.valueOf(dto.category()))
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return RestaurantResDto.from(savedRestaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResDto findRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND));
        return RestaurantResDto.from(restaurant);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResDto> findAllRestaurantByTitleAndCategory(
            String restaurantName,
            String category,
            Pageable pageable) { //todo: 주소 등 추가 검색 조건 파라미터 추가

        RestaurantCategory enumCategory = null;

        if (category != null && !category.isEmpty()) {
            enumCategory = RestaurantCategory.valueOf(category);
        }
        Page<Restaurant> restaurants = restaurantRepository.findAllRestaurantByTitleAndCategory(restaurantName,
                enumCategory, pageable);
        if (restaurants.isEmpty()) {
            throw CustomException.of(ErrorCode.NOT_FOUND, "해당 조건에 맞는 가게가 없습니다.");
        }
        return restaurants.map(RestaurantResDto::from);
    }

    @Transactional
    public RestaurantResDto updateRestaurantName(Long restaurantId, RestaurantNameUpdateReqDto dto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND));
        restaurant.updateRestaurantName(dto.restaurantName());
        return RestaurantResDto.from(restaurant);
    }

    @Transactional
    public void deleteRestaurantName(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND));
        restaurant.deleteRestaurant();
    }
}
