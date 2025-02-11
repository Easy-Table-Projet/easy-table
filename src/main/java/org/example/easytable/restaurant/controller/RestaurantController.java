package org.example.easytable.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.dto.request.CreateRestaurantDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantResDto> findRestaurantById (
            @PathVariable Long restaurantId){
        return ResponseEntity.ok(restaurantService.findRestaurantById(restaurantId));
    }


}
