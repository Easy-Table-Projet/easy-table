package org.example.easytable.config.streams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record StreamsOption(
        @Value("${redis.streams.map-record.key:reservation-key}")
        String key,

        @Value("${redis.streams.max-stream-length:1000}")
        long maxStreamLength,

        @Value("${redis.streams.count:5}")
        int streamCount
) { }
