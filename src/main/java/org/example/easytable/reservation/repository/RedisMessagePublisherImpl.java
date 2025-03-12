package org.example.easytable.reservation.repository;

import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.ProducerOption;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;

@Repository
@RequiredArgsConstructor
public class RedisMessagePublisherImpl implements MessagePublisher {
    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic topic;
    private final SerializerUtil<ReservationCreateReqDto> serializer;
    private final StreamsOption streamsOption;
    private final ProducerOption producerOption;

    @Override
    public void publish(ReservationCreateReqDto dto) throws TimeoutException {
        int attempt = 0;
        while (attempt < producerOption.maxAttempts()) {
            try {
                publishToStream(dto);
                return;
            } catch (Exception e) {
                ++attempt;
                sleepThread();
            }
        }

        throw new TimeoutException();
    }

    private void publishToStream(ReservationCreateReqDto dto) {
        String serialized = serializer.serialize(dto);
        Map<String, String> message = Collections.singletonMap(streamsOption.key(), serialized);

        XAddOptions options = XAddOptions.maxlen(streamsOption.maxStreamLength());
        Object addedId = redisTemplate.opsForStream().add(topic.getTopic(), message, options);

        if (addedId == null) {
            throw new RedisSystemException("스트림이 가득 차서 메시지를 추가할 수 없습니다.", new RuntimeException());
        }
    }

    private void sleepThread() {
        try {
            Thread.sleep(producerOption.retryDelayMillis());
        } catch (InterruptedException e) {
            System.out.println("thread interrupted");
        }
    }
}
