package org.example.easytable.queueing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;

import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.ProducerOption;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqMessage;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.repository.RedisMessagePublisherImpl;
import org.example.easytable.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.listener.ChannelTopic;

@ExtendWith(MockitoExtension.class)
public class RedisMessagePublisherTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ChannelTopic topic;
    @Mock
    private SerializerUtil<ReservationCreateReqMessage> serializer;
    @Mock
    private RestaurantRepository restaurantRepository;

    private final StreamsOption streamsOption = new StreamsOption("reservation-key", 1000, 5);
    private final ProducerOption producerOption = new ProducerOption(5, 500);

    private RedisMessagePublisherImpl publisher;

    @BeforeEach
    void setup() {
        publisher = new RedisMessagePublisherImpl(redisTemplate, topic, serializer, streamsOption, producerOption, restaurantRepository);
    }

    @Test
    void testPublishToStreamSuccess() throws TimeoutException {
        // given
        ReservationPostReqDto postReq = new ReservationPostReqDto(LocalDateTime.now());
        ReservationCreateReqMessage dto = ReservationCreateReqMessage.builder()
                .restaurantId(3L)
                .memberId(2L)
                .reservationPostReqDto(postReq)
                .build();

        String serialized = "{\"dummy\":\"data\"}";
        when(serializer.serialize(dto)).thenReturn(serialized);
        when(restaurantRepository.count()).thenReturn(10L); // 총 10개 레스토랑이라고 가정

        @SuppressWarnings("unchecked")
        StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);

        String expectedStreamKey = "reservation-create-2"; // ceil(3 * 5 / 10) = ceil(1.5) = 2
        when(streamOps.add(eq(expectedStreamKey), anyMap(), any(XAddOptions.class)))
                .thenReturn(RecordId.of("some-id"));

        // when
        publisher.publish(dto);

        // then
        verify(streamOps).add(eq(expectedStreamKey), anyMap(), any(XAddOptions.class));
    }

    @Test
    void testPublishToStreamFailure() {
        ReservationPostReqDto postReq = new ReservationPostReqDto(LocalDateTime.now());
        ReservationCreateReqMessage dto = ReservationCreateReqMessage.builder()
                .restaurantId(1L)
                .memberId(2L)
                .reservationPostReqDto(postReq)
                .build();

        String serialized = "{\"dummy\":\"data\"}";
        when(serializer.serialize(dto)).thenReturn(serialized);
        when(restaurantRepository.count()).thenReturn(10L);

        @SuppressWarnings("unchecked")
        StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);

        String expectedStreamKey = "reservation-create-1";
        when(streamOps.add(eq(expectedStreamKey), anyMap(), any(XAddOptions.class))).thenReturn(null);

        // then
        assertThrows(TimeoutException.class, () -> publisher.publish(dto));
    }
}
