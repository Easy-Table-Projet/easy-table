package org.example.easytable.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.dto.request.CreateRestaurantDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping//todo: 관리자 권한 설정 필요
    public ResponseEntity<RestaurantResDto> createRestaurant(
            @RequestBody CreateRestaurantDto dto){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(dto));
    }

}
