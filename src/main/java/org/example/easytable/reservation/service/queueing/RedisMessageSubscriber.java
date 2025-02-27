package org.example.easytable.reservation.service.queueing;

import io.lettuce.core.RedisBusyException;
import lombok.RequiredArgsConstructor;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
// Bean의 생명주기 관리를 통해 Consumer group 관리
public class RedisMessageSubscriber implements StreamListener<String, MapRecord<String, String, String>>, InitializingBean,
        DisposableBean {
    private static final String GROUP_NAME = "reservation-group";
    private static final String CONSUMER_NAME = "reservation-consumer";

    private final ChannelTopic topic;
    private final SinksRegistry sinkRegistry;
    private final SerializerUtil<ReservationCreateReqDto> serializerUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final ReservationService reservationService;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    private Subscription subscription;
    // TODO: 무분별한 @Value 정리하기
    @Value("${mapRecord-key:reservation-key}")
    private String key;
    @Value("${max-stream-length:100}")
    private long maxStreamLength;

    @Override
    public void afterPropertiesSet() {
        String groupName = topic.getTopic() + ":" + UUID.randomUUID();

        while (true) {
            try {
                redisTemplate.opsForStream().createGroup(groupName, ReadOffset.from("0"), GROUP_NAME);
                break;
            } catch (RedisBusyException e) {
                groupName = topic.getTopic() + ":" + UUID.randomUUID();
            }
        }
        redisTemplate.opsForStream().trim(groupName, maxStreamLength);

        this.subscription = listenerContainer.receive(
                Consumer.from(GROUP_NAME, CONSUMER_NAME),
                StreamOffset.create(topic.getTopic(), ReadOffset.lastConsumed()),
                this
        );

        this.listenerContainer.start();
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        ReservationCreateReqDto request = serializerUtil.deserialize(message.getValue().get(key));

        ReservationCreateResDto response = reservationService.createReservation(request);

        // SinkRegistry를 통해 요청을 보낸 스레드에 결과 전송
        sinkRegistry.completeSink(request.getRequestId(), response);
    }

    @Override
    public void destroy() throws Exception {
        if (subscription != null) {
            subscription.cancel();
        }

        if (listenerContainer != null) {
            listenerContainer.stop();
        }
    }
}
