package org.example.easytable.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Map;

import static org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;

@Repository
@RequiredArgsConstructor
public class RedisQueuePublisher implements QueuePublisher {
    private static final int MAX_ATTEMPTS = 5;
    private static final long RETRY_DELAY_MS = 500;

    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic topic;
    private final SerializerUtil<ReservationCreateReqDto> serializer;

    @Value("${mapRecord-key:reservation-key}")
    private String key;
    @Value("${max-stream-length:100}")
    private long maxStreamLength;

    @Override
    public void publish(ReservationCreateReqDto dto) {
        int attempt = 0;
        while (attempt < MAX_ATTEMPTS) {
            try {
                publishToStream(dto);
                return;
            } catch (RedisSystemException e) {
                ++attempt;
                sleepThread();
            }
        }

        throw new RuntimeException("스트림이 가득 차서 요청을 처리할 수 없습니다.");
    }

    private void publishToStream(ReservationCreateReqDto dto) {
        String serialized = serializer.serialize(dto);
        Map<String, String> message = Collections.singletonMap(key, serialized);

        XAddOptions options = XAddOptions.maxlen(maxStreamLength);
        Object addedId = redisTemplate.opsForStream().add(topic.getTopic(), message, options);

        if (addedId == null) {
            throw new RedisSystemException("스트림이 가득 차서 메시지를 추가할 수 없습니다.", new RuntimeException());
        }
    }

    private void sleepThread() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            System.out.println("thread interrupted");
        }
    }
}
