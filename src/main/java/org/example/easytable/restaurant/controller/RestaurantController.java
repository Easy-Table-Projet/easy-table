package org.example.easytable.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.dto.request.RestaurantCreateReqDto;
import org.example.easytable.restaurant.dto.request.RestaurantNameUpdateReqDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.service.RestaurantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping//todo: 관리자 권한 설정 필요
    public ResponseEntity<RestaurantResDto> createRestaurant(
            @RequestBody RestaurantCreateReqDto restaurantCreateReqDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(restaurantService.createRestaurant(restaurantCreateReqDto));
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantResDto> getRestaurantById(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(restaurantId));
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResDto>> getAllRestaurantByTitleAndCategory(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getAllRestaurantByTitleAndCategory(
                name, category,pageable));
    }

    @GetMapping("/top-100")
    public ResponseEntity<List<RestaurantResDto>> findTop100RestaurantList() {
        return ResponseEntity.ok(restaurantService.findTop100RestaurantList());
    }

    @PatchMapping("/{restaurantId}")
    public ResponseEntity<RestaurantResDto> updateRestaurantName(
            @PathVariable Long restaurantId,
            @RequestBody RestaurantNameUpdateReqDto restaurantNameUpdateReqDto) {
        return ResponseEntity.ok(restaurantService.updateRestaurantName(restaurantId, restaurantNameUpdateReqDto));
    }

    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable Long restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
