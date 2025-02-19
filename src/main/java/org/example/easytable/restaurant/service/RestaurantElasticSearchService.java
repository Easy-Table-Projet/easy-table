package org.example.easytable.restaurant.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.dto.response.RestaurantResDto;
import org.example.easytable.restaurant.entity.RestaurantDocument;
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

    public Page<RestaurantResDto> searchByFilters(String name, String category, Pageable pageable) {
        BoolQuery.Builder boolQuery = QueryBuilders.bool();

        List<Query> mustQueries = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            mustQueries.add(QueryBuilders.match().field("name").query(name).build()._toQuery());  // ✅ Query 변환 후 추가
        }

        if (category != null && !category.isEmpty()) {
            mustQueries.add(QueryBuilders.term().field("category").value(category).build()._toQuery());  // ✅ Query
        }

        mustQueries.add(QueryBuilders.term().field("isDeleted").value(false).build()._toQuery());  // ✅ Query 변환 후 추가

        boolQuery.must(mustQueries);

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

}
