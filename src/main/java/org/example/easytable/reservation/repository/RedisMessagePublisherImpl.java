package org.example.easytable.reservation.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.ProducerOption;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
import org.example.easytable.restaurant.repository.RestaurantRepository;
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
@Slf4j
public class RedisMessagePublisherImpl implements MessagePublisher {
    private final RedisTemplate<String, String> redisTemplate;
    private final ChannelTopic topic;
    private final SerializerUtil<ReservationCreateReqMessage> serializer;
    private final StreamsOption streamsOption;
    private final ProducerOption producerOption;
    private final RestaurantRepository restaurantRepository;

    @Override
    public void publish(ReservationCreateReqMessage dto) throws TimeoutException {
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

    private XAddOptions createXAddOptions() {
        return XAddOptions.maxlen(streamsOption.maxStreamLength());
    }

    private void publishToStream(ReservationCreateReqMessage dto) {
        Map<String, String> message = Collections.singletonMap(streamsOption.key(), serializer.serialize(dto));

        String streamKey = createStreamKey(dto.getRestaurantId());

        Object addedId = redisTemplate.opsForStream().add(
                streamKey, message, createXAddOptions());
        log.info("published message into {}", streamKey);

        if (addedId == null) {
            throw new RedisSystemException("스트림이 가득 차서 메시지를 추가할 수 없습니다.", new RuntimeException());
        }

        System.out.println("finished publishing");
    }

    private void sleepThread() {
        try {
            Thread.sleep(producerOption.retryDelayMillis());
        } catch (InterruptedException e) {
            System.out.println("thread interrupted");
        }
    }

    private String createStreamKey(Long restaurantId) {
        long streamNumber = restaurantId % streamsOption.streamCount() + 1;
        return "reservation-create-" + streamNumber;
    }
}
