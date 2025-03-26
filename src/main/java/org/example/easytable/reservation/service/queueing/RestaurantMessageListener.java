package org.example.easytable.reservation.service.queueing;

import lombok.extern.slf4j.Slf4j;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;

@Slf4j
public class RestaurantMessageListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final String restaurantId;
    private final String streamKey;
    private final String groupName;

    private final RedisTemplate<String, String> redisTemplate;
    private final SerializerUtil<ReservationCreateReqMessage> serializerUtil;
    private final ReservationService reservationService;
    private final SinksRegistry sinkRegistry;
    private final StreamsOption streamsOption;

    /**
     * 생성 시점에 각종 의존성 및 레스토랑에 대한 정보를 주입받습니다.
     *
     * @param restaurantId 해당 레스토랑의 ID
     * @param streamKey  이 레스토랑의 스트림 키
     * @param groupName  이 레스토랑에 할당된 Consumer Group 이름
     * @param redisTemplate RedisTemplate 인스턴스
     * @param serializerUtil 메시지 직렬화/역직렬화 유틸리티
     * @param reservationService 예약 생성 서비스
     * @param sinkRegistry 요청 결과 전달용 SinkRegistry
     * @param streamsOption 스트림 옵션 (ex: key 이름, 최대 길이 등)
     */
    public RestaurantMessageListener(
            String restaurantId,
            String streamKey,
            String groupName,
            RedisTemplate<String, String> redisTemplate,
            SerializerUtil<ReservationCreateReqMessage> serializerUtil,
            ReservationService reservationService,
            SinksRegistry sinkRegistry,
            StreamsOption streamsOption
    ) {
        this.restaurantId = restaurantId;
        this.streamKey = streamKey;
        this.groupName = groupName;
        this.redisTemplate = redisTemplate;
        this.serializerUtil = serializerUtil;
        this.reservationService = reservationService;
        this.sinkRegistry = sinkRegistry;
        this.streamsOption = streamsOption;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        // streamsOption.key()로 지정한 필드의 값을 역직렬화하여 메시지 객체 생성
        String messageFieldKey = streamsOption.key();
        ReservationCreateReqMessage request = serializerUtil.deserialize(
                message.getValue().get(messageFieldKey)
        );

        log.debug("streamKey: {}, groupName: {}", streamKey, groupName);

        try {
            // ReservationService를 통해 예약 생성 및 결과 획득
            ReservationCreateResDto response = reservationService.createReservation(request);
            // SinkRegistry를 이용하여 요청을 보낸 곳에 결과 전달

            sinkRegistry.completeSink(request.getRequestId(), response);
            // 성공적으로 처리된 메시지에 대해 acknowledge 호출
            redisTemplate.opsForStream().acknowledge(streamKey, groupName, message.getId());
        } catch (Exception e) {
            log.error("레스토랑 [{}] 메시지 처리 오류: {}", restaurantId, e.getMessage());
            sinkRegistry.completeSinkExceptionally(request.getRequestId(), e);
        }
    }
}
