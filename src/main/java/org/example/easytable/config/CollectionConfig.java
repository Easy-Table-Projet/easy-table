package org.example.easytable.config;

import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Configuration
public class CollectionConfig {
    @Value("${queue-capacity:25}")
    private int capacity;

    @Bean
    public BlockingQueue<ReservationCreateReqDto> blockingQueue() {
        return new ArrayBlockingQueue<>(capacity);
    }


}
