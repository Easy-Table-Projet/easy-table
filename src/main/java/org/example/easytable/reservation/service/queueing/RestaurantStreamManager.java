package org.example.easytable.reservation.service.queueing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.ConsumerGroupOption;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
import org.example.easytable.reservation.service.ReservationService;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.example.easytable.restaurant.service.RestaurantService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestaurantStreamManager implements InitializingBean, DisposableBean {
    // 레스토랑 목록 제공 (초기 등록된 레스토랑 ID 목록을 반환한다고 가정)
    private final RestaurantService restaurantService;

    private final RestaurantRepository restaurantRepository;

    private final RedisTemplate<String, String> redisTemplate;

    // 각 레스토랑에 대해 생성되는 여러 Subscription을 관리
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    // listenerContainer는 여러 스트림을 동시에 구독할 수 있음
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    // 각 스트림 메시지의 직렬화/역직렬화를 위한 유틸리티
    private final SerializerUtil<ReservationCreateReqMessage> serializerUtil;

    // 예약 처리 서비스 ( 각 메시지를 받고 실제 작업 수행 )
    private final ReservationService reservationService;

    // SinkRegistry를 통해 요청자에게 결과를 반환
    private final SinksRegistry sinkRegistry;

    // 스트림 관련 옵션 (예: 최대 stream 길이, 키 이름 등)
    private final StreamsOption streamsOption;

    // 기본 Consumer group 관련 옵션 (시작 오프셋 등)
    private final ConsumerGroupOption groupOption;

    /**
     * 애플리케이션 초기화 시 기존 레스토랑 목록에 대해
     * 각 레스토랑 전용 Stream, Consumer group을 생성 및 구독을 시작합니다.
     */
    @Override
    public void afterPropertiesSet() {
        // 초기 레스토랑 ID 목록 취득 (예, DB에 등록된 모든 restaurantId)
        List<Long> restaurantIds = restaurantRepository.findAllRestaurantIds();
        for (Long restaurantId : restaurantIds) {
            addRestaurantStream(String.valueOf(restaurantId));
        }
        listenerContainer.start();
    }

    /**
     * 애플리케이션 종료 시 모든 구독 취소 및 listenerContainer 종료
     */
    @Override
    public void destroy() {
        subscriptions.values().forEach(Subscription::cancel);
        if (listenerContainer != null) {
            listenerContainer.stop();
        }
    }

    /**
     * 외부(레스토랑 추가 이벤트 처리 Service)에서 호출되어 새로운 레스토랑에 대해
     * Stream 및 Consumer group 생성, 구독을 시작합니다.
     */
    public void addRestaurantStream(String restaurantId) {
        String streamKey = getStreamKey(restaurantId);
        String groupName = getGroupName(restaurantId);
        String consumerName = getConsumerName(restaurantId);

        // 지정된 streamKey에 consumer group 생성 (이미 존재하면 BUSYGROUP 예외 처리)
        createConsumerGroup(streamKey, groupName);

        // 스트림 길이 제한 적용 (옵션 사용)
        redisTemplate.opsForStream().trim(streamKey, streamsOption.maxStreamLength());

        // 별도의 RestaurantMessageListener 인스턴스 생성 후 구독 등록
        RestaurantMessageListener listener = new RestaurantMessageListener(
                restaurantId,
                streamKey,
                groupName,
                redisTemplate,
                serializerUtil,
                reservationService,
                sinkRegistry,
                streamsOption
        );

        Subscription subscription = listenerContainer.receive(
                Consumer.from(groupName, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                listener
        );
        subscriptions.put(restaurantId, subscription);
        log.info("레스토랑 [{}] 스트림 구독 생성됨. [streamKey={}, groupName={}, consumerName={}]",
                restaurantId, streamKey, groupName, consumerName);
    }

    /**
     * 외부(레스토랑 삭제 이벤트 처리 Service)에서 호출되어 삭제 대상 레스토랑의
     * Stream & Consumer group 구독을 취소/제거합니다.
     */
    public void removeRestaurantStream(String restaurantId) {
        Subscription subscription = subscriptions.remove(restaurantId);
        if (subscription != null) {
            subscription.cancel();
        }
        String streamKey = getStreamKey(restaurantId);
        String groupName = getGroupName(restaurantId);
        // Consumer group 삭제 (Redis 버전에 따라 지원되지 않으면 별도 관리)
        try {
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                byte[] keyBytes = streamKey.getBytes(StandardCharsets.UTF_8);
                return connection.streamCommands().xGroupDestroy(keyBytes, groupName);
            });
        } catch (Exception e) {
            log.error("레스토랑 [{}] Consumer group 삭제 실패: {}", restaurantId, e.getMessage());
        }
        log.info("레스토랑 [{}] 스트림 구독 및 Consumer group 제거됨.", restaurantId);
    }

    // ------------------ 유틸 메서드 ------------------

    /**
     * restaurantId를 이용하여 해당 스트림의 키 생성
     */
    private String getStreamKey(String restaurantId) {
        return "restaurant:stream:" + restaurantId;
    }

    /**
     * restaurantId를 이용하여 해당 Consumer group의 이름 생성
     */
    private String getGroupName(String restaurantId) {
        return "restaurant:group:" + restaurantId;
    }

    /**
     * restaurantId를 기반으로 고유한 consumer name 생성
     */
    private String getConsumerName(String restaurantId) {
        return "restaurant:consumer:" + restaurantId + ":" + UUID.randomUUID().toString();
    }

    /**
     * 레디스에 해당 streamKey에 consumer group을 생성합니다.
     */
    private void createConsumerGroup(String streamKey, String groupName) {
        try {
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                byte[] keyBytes = streamKey.getBytes(StandardCharsets.UTF_8);
                // 시작 오프셋은 groupOption에서 지정한 값 또는 기본값 사용
                ReadOffset offset = ReadOffset.from(groupOption.readOffset());
                return Boolean.valueOf(connection.streamCommands().xGroupCreate(keyBytes, groupName, offset, true));
            });
        } catch (RedisSystemException e) {
            // 이미 그룹이 존재하는 경우 BUSYGROUP 오류 무시
            if (e.getCause() == null || !e.getCause().getMessage().contains("BUSYGROUP")) {
                throw e;
            }
        }
    }
}
