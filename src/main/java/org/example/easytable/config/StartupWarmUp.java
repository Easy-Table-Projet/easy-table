package org.example.easytable.config;

import lombok.RequiredArgsConstructor;
import org.example.easytable.restaurant.service.RestaurantElasticSearchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupWarmUp implements CommandLineRunner {
    private final RestaurantElasticSearchService warmUpService;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Elasticsearch 워밍업 실행 중...");
        warmUpService.warmUpElasticsearch();
    }
}
