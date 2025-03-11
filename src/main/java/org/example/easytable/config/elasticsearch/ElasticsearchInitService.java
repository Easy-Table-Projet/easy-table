package org.example.easytable.config.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.entity.RestaurantDocument;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ElasticsearchInitService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    public void warmUpElasticsearch() {
        NativeQuery warmUpQuery = NativeQuery.builder()
                .withQuery(QueryBuilders.matchAll().build()._toQuery()) // 모든 문서 조회
                .withPageable(org.springframework.data.domain.PageRequest.of(0, 1)) // 첫 페이지 1개만 가져오기
                .build();

        elasticsearchTemplate.search(warmUpQuery, RestaurantDocument.class);

        System.out.println("✅ Elasticsearch 워밍업 완료!");
    }


}
