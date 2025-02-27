package org.example.easytable.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisQueuePublisher implements QueuePublisher {
    private final RedisTemplate<String, ReservationCreateReqDto> redisTemplate;
    private final ChannelTopic topic;
    private final SerializerUtil<ReservationCreateReqDto> serializer;

    @Override
    public void publish(ReservationCreateReqDto dto) {
        redisTemplate.convertAndSend(topic.getTopic(), serializer.serialize(dto));
    }
}
