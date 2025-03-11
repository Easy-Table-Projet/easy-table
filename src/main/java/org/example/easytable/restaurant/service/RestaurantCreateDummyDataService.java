package org.example.easytable.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.example.easytable.restaurant.entity.RestaurantDocument;
import org.example.easytable.restaurant.repository.RestaurantElasticSearchRepository;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RestaurantCreateDummyDataService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantElasticSearchRepository elasticSearchRepository;
    private final MemberRepository memberRepository;

    private static final List<String> restaurantNames = List.of(
            "맛있는 식당", "인기 오마카세", "최고급 한우 전문점", "엄마손 밥상", "수제 버거 하우스",
            "전통 손칼국수", "이태리 피자 전문점", "일본 라멘 맛집", "신선한 해산물 요리", "가성비 갑 스테이크",
            "불향 가득한 고깃집", "서울 최고의 막국수", "전통 수제비 전문점", "바삭한 돈가스 명가", "신선한 사시미 하우스",
            "유럽 감성 브런치 카페", "프렌치 파인 다이닝", "정통 이탈리안 레스토랑", "베트남 쌀국수 맛집", "태국 정통 팟타이",
            "남미 스타일 스테이크 하우스", "홍콩 딤섬 전문점", "광둥식 바베큐 하우스", "베이징 오리구이", "정통 사천요리",
            "대만 샤오롱바오 맛집", "뉴욕 스타일 피자", "LA 핫도그 전문점", "미국식 바비큐 스모크 하우스", "멕시코 타코 & 부리또",
            "스페인 전통 빠에야", "아르헨티나 스테이크 하우스", "브라질 슈하스코", "터키 케밥 전문점", "그리스 전통 요리",
            "인도 커리 & 탄두리", "신선한 마라탕 전문점", "정통 마라샹궈", "푸짐한 훠궈 전문점", "모던 한식 다이닝",
            "제주 흑돼지 전문점", "전국 1등 곰탕집", "담백한 설렁탕 명가", "30년 전통 순댓국", "수제 막창 & 곱창",
            "한옥 감성 한정식", "유기농 샐러드 바", "건강한 비건 레스토랑", "로컬 파스타 & 리조또", "트러플 전문 다이닝",
            "부산 명물 회 센터", "제주 해산물 한 상", "싱가포르 칠리크랩", "홍콩식 완탕면", "도쿄 스타일 이자카야",
            "오사카 다코야끼 전문점", "후쿠오카 모츠나베", "홋카이도 스프 카레", "산동식 마라탕면", "사천식 마라두부",
            "광둥식 새우 딤섬", "이태리 정통 젤라또", "파리 감성 베이커리", "도쿄 수제 케이크 카페", "뉴욕 치즈케이크 전문점",
            "프랑스 정통 크루아상", "전통 한국식 팥빙수", "대만 버블티 카페", "홍콩 에그타르트", "LA 스타일 스무디 바",
            "서울 핫한 디저트 카페", "수제 초콜릿 & 마카롱", "100% 수제 크로플", "전통 카페 & 전통차",
            "강릉 커피 명가", "브라질 스페셜티 커피", "베트남 연유 커피", "정통 에스프레소 바", "스페인 츄러스 & 핫초코",
            "나폴리 스타일 에스프레소", "수제 버블티 & 과일주스", "멕시코 과카몰레 전문점", "레바논 지중해 음식",
            "신선한 스시 & 롤바", "최고급 텐동 전문점", "서울 No.1 스테이크 하우스", "30년 전통 닭한마리",
            "정통 동남아 스트리트 푸드", "이색 타코 & 칵테일 바", "글로벌 비건 레스토랑", "한강 뷰 감성 카페",
            "럭셔리 호텔 뷔페", "무제한 삼겹살 맛집", "가성비 좋은 한우 집", "프리미엄 한우 오마카세", "서울 핫한 퓨전 다이닝"
    );
    private static final Random random = new Random();
    public static String getRandomRestaurantName() {
        return restaurantNames.get(random.nextInt(restaurantNames.size()));
    }

    public static RestaurantCategory getRandomCategory() {
        RestaurantCategory[] categories = RestaurantCategory.values();
        return categories[random.nextInt(categories.length)];
    }

    @Transactional
    public void bulkInsertRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        Long memberId = AuthUtil.getId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다"));


        for (int i = 0; i < 10000; i++) {
            Restaurant restaurant = Restaurant.builder()
                    .name(getRandomRestaurantName() + " " + (i + 1)) // 식당1, 식당2...
                    .address("서울시 강남구 " + (i + 1))
                    .maxTableCount(10 + (i % 5))
                    .category(getRandomCategory())
                    .owner(member)
                    .build();

            restaurants.add(restaurant);
        }

        // 1. MySQL에 저장
        restaurantRepository.saveAll(restaurants);

        // 2. 엘라스틱서치에 저장
        List<RestaurantDocument> docs = restaurants.stream()
                .map(RestaurantDocument::from)
                .toList();

        elasticSearchRepository.saveAll(docs);

    }

}
