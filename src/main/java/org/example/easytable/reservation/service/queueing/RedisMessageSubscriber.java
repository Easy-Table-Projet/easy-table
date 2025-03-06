package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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

        String streamKey = topic.getTopic();

        try {
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                // 스트림 키, 그룹 이름, 시작 offset을 바이트 배열로 변환합니다.
                byte[] keyBytes = streamKey.getBytes(StandardCharsets.UTF_8);
                ReadOffset offsetBytes = ReadOffset.from("0");

                // streamCommands()를 통해 group 생성을 수행합니다.
                return Boolean.valueOf(
                        connection.streamCommands().xGroupCreate(keyBytes, GROUP_NAME, offsetBytes, true));
            });
        } catch (DataAccessException e) {
            // 이미 그룹이 존재하는 경우 Redis는 "BUSYGROUP" 메시지를 포함한 에러를 반환하므로 이를 무시합니다.
            if (!e.getMessage().contains("BUSYGROUP")) {
                throw e;
            }
        }


        redisTemplate.opsForStream().trim(streamKey, maxStreamLength);

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
