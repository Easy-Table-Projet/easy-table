//package org.example.easytable.queueing;
//
//import org.example.easytable.common.utils.SerializerUtil;
//import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
//import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
//import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
//import org.example.easytable.reservation.entity.ReservationStatus;
//import org.example.easytable.reservation.service.ReservationService;
//import org.example.easytable.reservation.service.queueing.RedisMessageSubscriber;
//import org.example.easytable.reservation.service.queueing.SinksRegistry;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.connection.stream.MapRecord;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.listener.ChannelTopic;
//import org.springframework.data.redis.stream.StreamMessageListenerContainer;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class RedisMessageSubscriberTest {
//    @Mock
//    private ChannelTopic topic;
//    @Mock
//    private SinksRegistry sinkRegistry;
//    @Mock
//    private SerializerUtil<ReservationCreateReqDto> serializerUtil;
//    @Mock
//    private RedisTemplate<String, String> redisTemplate;
//    @Mock
//    private ReservationService reservationService;
//    @Mock
//    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
//
//    private RedisMessageSubscriber subscriber;
//
//    @BeforeEach
//    void setup() {
//        subscriber = new RedisMessageSubscriber(
//                topic, sinkRegistry, serializerUtil, redisTemplate, reservationService, listenerContainer);
//        // @Value로 주입되는 필드 설정
//        ReflectionTestUtils.setField(subscriber, "key", "reservation-key");
//        ReflectionTestUtils.setField(subscriber, "maxStreamLength", 100L);
//    }
//
//    @Test
//    void testOnMessage() {
//        // given
//        String serializedData = "{\"dummy\":\"data\"}";
//        Map<String, String> valueMap = new HashMap<>();
//        valueMap.put("reservation-key", serializedData);
//
//        ReservationCreateReqDto request = ReservationCreateReqDto.builder()
//                .restaurantId(1L)
//                .memberId(2L)
//                .reservationPostReqDto(new ReservationPostReqDto(LocalDateTime.now()))
//                .build();
//
//        ReservationCreateResDto resDto = new ReservationCreateResDto(
//            request.getRequestId(),
//                1L,
//                2L,
//                1L,
//                request.getReservationPostReqDto().reservationTime(),
//                ReservationStatus.CONFIRMED
//        );
//
//        // when
//        when(serializerUtil.deserialize(serializedData)).thenReturn(request);
//        when(reservationService.createReservation(request)).thenReturn(resDto);
//
//        @SuppressWarnings("unchecked")
//        MapRecord<String, String, String> message = mock(MapRecord.class);
//        when(message.getValue()).thenReturn(valueMap);
//
//        subscriber.onMessage(message);
//
//        // then
//        // sinkRegistry.completeSink()가 올바른 파라미터로 호출되었는지 검증
//        verify(sinkRegistry).completeSink(request.getRequestId(), resDto);
//    }
//}
