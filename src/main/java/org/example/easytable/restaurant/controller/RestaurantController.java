package org.example.easytable.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.dto.request.RestaurantCreateDto;
import org.example.easytable.restaurant.dto.request.RestaurantNameUpdateReqDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.service.RestaurantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            @RequestBody RestaurantCreateDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(dto));
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantResDto> findRestaurantById(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.findRestaurantById(restaurantId));
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResDto>> findAllRestaurantByTitleAndCategory(
            @RequestParam(required = false) String restaurantName,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        return ResponseEntity.ok(restaurantService.findAllRestaurantByTitleAndCategory(restaurantName,category, pageable));
    }

    @PatchMapping("/{restaurantId}")//todo: 관리자 권한 설정 필요
    public ResponseEntity<RestaurantResDto> updateRestaurantName(
            @PathVariable Long restaurantId,
            @RequestBody RestaurantNameUpdateReqDto dto) {
        return ResponseEntity.ok(restaurantService.updateRestaurantName(restaurantId, dto));
    }

    @DeleteMapping("/{restaurantId}")//todo: 관리자 권한 설정 필요
    public ResponseEntity<Void> deleteRestaurantName(
            @PathVariable Long restaurantId) {
        restaurantService.deleteRestaurantName(restaurantId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
