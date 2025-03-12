package org.example.easytable.config.streams;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record ProducerOption(
        @Value("${redis.streams.producer.max-attempts:5}")
        int maxAttempts,

        @Value("${redis.streams.producer.ms:500}")
        long retryDelayMillis
) { }
