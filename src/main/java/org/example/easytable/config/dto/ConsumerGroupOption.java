package org.example.easytable.config.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record ConsumerGroupOption(
        @Value("${redis.streams.consumer_group.read_offset:0}")
        String readOffset,

        @Value("${redis.streams.consumer_group.name:reservation-group}")
        String groupName,

        @Value("${redis.streams.consumer_group.consumer_name:reservation-consumer}")
        String consumerName
) { }
