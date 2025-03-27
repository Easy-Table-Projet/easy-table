package org.example.easytable.queueing;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.ConsumerGroupOption;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.entity.ReservationStatus;
import org.example.easytable.reservation.service.ReservationService;
import org.example.easytable.reservation.service.queueing.RedisMessageSubscriber;
import org.example.easytable.reservation.service.queueing.SinksRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

@ExtendWith(MockitoExtension.class)
public class RedisMessageSubscriberTest {
    @Mock
    private ChannelTopic topic;
    @Mock
    private SinksRegistry sinkRegistry;
    @Mock
    private SerializerUtil<ReservationCreateReqMessage> serializerUtil;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ReservationService reservationService;
    @Mock
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;

    private final StreamsOption streamsOption = new StreamsOption("reservation-key", 1000, 5);
    private final ConsumerGroupOption consumerGroupOption = new ConsumerGroupOption(
            "0", "reservation-group", "reservation-consumer");

    private RedisMessageSubscriber subscriber;

    @BeforeEach
    void setup() {
        subscriber = new RedisMessageSubscriber(
                topic, sinkRegistry, serializerUtil, redisTemplate, reservationService,
                listenerContainer, streamsOption, consumerGroupOption);
    }

    @Test
    void testOnMessage() {
        // given
        String serializedData = "{\"dummy\":\"data\"}";
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("reservation-key", serializedData);

        ReservationCreateReqMessage request = ReservationCreateReqMessage.builder()
                .restaurantId(1L)
                .memberId(2L)
                .reservationPostReqDto(new ReservationPostReqDto(LocalDateTime.now()))
                .build();

        ReservationCreateResDto resDto = new ReservationCreateResDto(
                request.getRequestId(),
                1L,
                2L,
                1L,
                request.getReservationPostReqDto().reservationTime(),
                ReservationStatus.CONFIRMED
        );

        @SuppressWarnings("unchecked")
        StreamOperations<String, String, String> streamOps = mock(StreamOperations.class);
        doReturn(streamOps).when(redisTemplate).opsForStream();

        @SuppressWarnings("unchecked")
        MapRecord<String, String, String> message = mock(MapRecord.class);
        when(message.getValue()).thenReturn(valueMap);
        when(message.getStream()).thenReturn("reservation-create-1");
        when(message.getId()).thenReturn(RecordId.autoGenerate());

        // when
        when(serializerUtil.deserialize(serializedData)).thenReturn(request);
        when(reservationService.createReservation(request)).thenReturn(resDto);

        subscriber.onMessage(message);

        // then
        verify(sinkRegistry).completeSink(request.getRequestId(), resDto);
        verify(redisTemplate.opsForStream()).acknowledge("reservation-create-1", "reservation-group", message.getId());
    }
}
