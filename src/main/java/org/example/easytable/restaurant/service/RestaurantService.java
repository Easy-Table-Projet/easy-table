package org.example.easytable.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.restaurant.dto.request.RestaurantCreateReqDto;
import org.example.easytable.restaurant.dto.request.RestaurantNameUpdateReqDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.example.easytable.restaurant.entity.RestaurantDocument;
import org.example.easytable.restaurant.repository.RestaurantElasticSearchRepository;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantElasticSearchRepository elasticSearchRepository;
    private static final String CACHE_KEY = "top100Restaurants";
    private final MemberRepository memberRepository;

    @Transactional
    public RestaurantResDto createRestaurant(RestaurantCreateReqDto restaurantCreateReqDto) {
        Long memberId = AuthUtil.getId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다"));

        Restaurant restaurant = Restaurant.builder()
                .name(restaurantCreateReqDto.name())
                .address(restaurantCreateReqDto.address())
                .maxTableCount(restaurantCreateReqDto.maxTableCount())
                .category(RestaurantCategory.valueOf(restaurantCreateReqDto.category()))
                .owner(member)
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        RestaurantDocument document = RestaurantDocument.from(savedRestaurant);
        elasticSearchRepository.save(document);
        return RestaurantResDto.from(savedRestaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResDto getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND));
        return RestaurantResDto.from(restaurant);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResDto> getAllRestaurantByTitleAndCategory(
            String name,
            String category,
            Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findAllRestaurantByTitleAndCategory(
                name, category, pageable);

        return restaurants.map(RestaurantResDto::from);
    }

    @Cacheable(value = CACHE_KEY)
    @Transactional(readOnly = true)
    public List<RestaurantResDto> findTop100RestaurantList() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Restaurant> restaurants = restaurantRepository.findTop100RestaurantList(oneMonthAgo);
        return restaurants.stream()
                .map(RestaurantResDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantResDto updateRestaurantName(Long restaurantId,
                                                 RestaurantNameUpdateReqDto restaunrantNameUpdateReqDto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 레스토랑입니다"));

        validateOwnership(restaurant);

        restaurant.updateName(restaunrantNameUpdateReqDto.name());
        RestaurantDocument document = RestaurantDocument.from(restaurant);
        elasticSearchRepository.save(document);
        return RestaurantResDto.from(restaurant);
    }

    @Transactional
    public void deleteRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 레스토랑입니다"));
        validateOwnership(restaurant);
        elasticSearchRepository.deleteById(restaurant.getId());
        restaurant.softDelete();
    }

    private void validateOwnership(Restaurant restaurant) {
        Long currentUserId = AuthUtil.getId();
        if (!restaurant.getOwner().getId().equals(currentUserId)) {
            throw CustomException.of(ErrorCode.FORBIDDEN, "해당 레스토랑의 소유자가 아닙니다");
        }
    }

}