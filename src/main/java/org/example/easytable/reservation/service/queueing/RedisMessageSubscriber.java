package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.ConsumerGroupOption;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
// Bean의 생명주기 관리를 통해 Consumer group 관리
public class RedisMessageSubscriber implements StreamListener<String, MapRecord<String, String, String>>, InitializingBean,
        DisposableBean {
    private final ChannelTopic topic;
    private final SinksRegistry sinkRegistry;
    private final SerializerUtil<ReservationCreateReqDto> serializerUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final ReservationService reservationService;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final StreamsOption streamsOption;
    private final ConsumerGroupOption groupOption;

    private Subscription subscription;

    @Override
    public void afterPropertiesSet() {

        String streamKey = topic.getTopic();

        try {
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                byte[] keyBytes = streamKey.getBytes(StandardCharsets.UTF_8);
                ReadOffset offsetBytes = ReadOffset.from(groupOption.readOffset());

                // Consumer group 생성
                return Boolean.valueOf(
                        connection.streamCommands().xGroupCreate(keyBytes, groupOption.groupName(), offsetBytes, true));
            });
        } catch (RedisSystemException e) {
            if (e.getCause() == null || !e.getCause().getMessage().contains("BUSYGROUP")) { throw e; }
        }

        redisTemplate.opsForStream().trim(streamKey, streamsOption.maxStreamLength());

        this.subscription = listenerContainer.receive(
                Consumer.from(groupOption.groupName(), groupOption.consumerName()),
                StreamOffset.create(topic.getTopic(), ReadOffset.lastConsumed()),
                this
        );

        this.listenerContainer.start();
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        ReservationCreateReqDto request = serializerUtil.deserialize(message.getValue().get(streamsOption.key()));

        try {
            ReservationCreateResDto response = reservationService.createReservation(request);
            // SinkRegistry를 통해 요청을 보낸 스레드에 결과 전송
            sinkRegistry.completeSink(request.getRequestId(), response);
            redisTemplate.opsForStream().acknowledge(topic.getTopic(), groupOption.groupName(), message.getId());
        } catch (Exception e) {
            log.error(e.getMessage());
            sinkRegistry.completeSinkExceptionally(request.getRequestId(), e);
        }
    }

    @Override
    public void destroy() {
        if (subscription != null) {
            subscription.cancel();
        }

        if (listenerContainer != null) {
            listenerContainer.stop();
        }
    }
}
