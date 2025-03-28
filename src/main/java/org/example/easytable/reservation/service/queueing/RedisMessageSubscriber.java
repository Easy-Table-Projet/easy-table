package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.ConsumerGroupOption;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
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
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber implements
        StreamListener<String, MapRecord<String, String, String>>, InitializingBean,
        DisposableBean {

    private final ChannelTopic topic;
    private final SinksRegistry sinkRegistry;
    private final SerializerUtil<ReservationCreateReqMessage> serializerUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final ReservationService reservationService;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final StreamsOption streamsOption;
    private final ConsumerGroupOption groupOption;

    private final List<Subscription> subscriptions = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        for (int i = 1; i <= streamsOption.streamCount(); i++) {
            String streamKey = "reservation-create-" + i;

            createConsumerGroup(streamKey);
            redisTemplate.opsForStream().trim(streamKey, streamsOption.maxStreamLength());

            Subscription sub = listenerContainer.receive(
                    Consumer.from(groupOption.groupName(), groupOption.consumerName()),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                    this
            );

            subscriptions.add(sub);
        }

        this.listenerContainer.start();
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        ReservationCreateReqMessage request = serializerUtil.deserialize(message.getValue().get(streamsOption.key()));

        try {
            ReservationCreateResDto response = reservationService.createReservation(request);
            log.debug("completing {}", request.getRequestId());
            sinkRegistry.completeSink(request.getRequestId(), response);
            redisTemplate.opsForStream().acknowledge(message.getStream(), groupOption.groupName(), message.getId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            sinkRegistry.completeSinkExceptionally(request.getRequestId(), e);
        }
    }

    @Override
    public void destroy() {
        subscriptions.forEach(Subscription::cancel);

        if (listenerContainer != null) {
            listenerContainer.stop();
        }
    }

    private void createConsumerGroup(String streamKey) {
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
    }

}