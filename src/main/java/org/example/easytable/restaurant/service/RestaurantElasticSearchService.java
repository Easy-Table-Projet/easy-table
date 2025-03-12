package org.example.easytable.restaurant.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.AuthUtil;
import org.example.easytable.exception.CustomException;
import org.example.easytable.exception.ErrorCode;
import org.example.easytable.member.entity.Member;
import org.example.easytable.member.repository.MemberRepository;
import org.example.easytable.restaurant.dto.request.RestaurantCreateReqDto;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.Restaurant;
import org.example.easytable.restaurant.entity.RestaurantCategory;
import org.example.easytable.restaurant.entity.RestaurantDocument;
import org.example.easytable.restaurant.repository.RestaurantElasticSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantElasticSearchService {
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final MemberRepository memberRepository;
    private final RestaurantElasticSearchRepository elasticSearchRepository;

    public Page<RestaurantResDto> searchByFilters(String name, String category, Pageable pageable) {
        BoolQuery.Builder boolQuery = QueryBuilders.bool();

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            mustQueries.add(QueryBuilders.multiMatch()
                    .fields("name^2.5", "name.keyword^3", "name.ngram^1.5", "name.edge_ngram^1.5")
                    .query(name)
                    .build()._toQuery());
        }

        if (category != null && !category.isEmpty()) {
            filterQueries.add(QueryBuilders.term().field("category").value(category).build()._toQuery());  // ✅ Query
        }

        filterQueries.add(QueryBuilders.term().field("isDeleted").value(false).build()._toQuery());  // ✅ Query 변환 후 추가
        if (!mustQueries.isEmpty()) {
            boolQuery.must(mustQueries);
        }
        boolQuery.filter(filterQueries);

        Query query = boolQuery.build()._toQuery();


        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .build();

        SearchHits<RestaurantDocument> searchHits = elasticsearchTemplate.search(searchQuery, RestaurantDocument.class);

        List<RestaurantResDto> restaurantList = searchHits.stream()
                .map(hit -> RestaurantResDto.from(hit.getContent()))
                .toList();

        return new PageImpl<>(restaurantList, pageable, searchHits.getTotalHits());
    }

    public RestaurantResDto createRestaurantEs(RestaurantCreateReqDto restaurantCreateReqDto) {
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

        RestaurantDocument document = RestaurantDocument.from(restaurant);
        elasticSearchRepository.save(document);
        return RestaurantResDto.from(restaurant);
    }
}
