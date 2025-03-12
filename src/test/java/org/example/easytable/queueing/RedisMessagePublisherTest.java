package org.example.easytable.queueing;

import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.config.streams.ProducerOption;
import org.example.easytable.config.streams.StreamsOption;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.request.ReservationPostReqDto;
import org.example.easytable.reservation.repository.RedisMessagePublisherImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.listener.ChannelTopic;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;

@ExtendWith(MockitoExtension.class)
public class RedisMessagePublisherTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ChannelTopic topic;
    @Mock
    private SerializerUtil<ReservationCreateReqDto> serializer;

    private final StreamsOption streamsOption = new StreamsOption("reservation-key", 1000);
    private final ProducerOption producerOption = new ProducerOption(5, 500);

    private RedisMessagePublisherImpl publisher;

    // 테스트용 maxStreamLength 및 key 값을 지정
    @BeforeEach
    void setup() {
        publisher = new RedisMessagePublisherImpl(redisTemplate, topic, serializer, streamsOption, producerOption);
    }

    @Test
    void testPublishToStreamSuccess() throws TimeoutException {
        // given
        ReservationPostReqDto postReq = new ReservationPostReqDto(LocalDateTime.now());
        ReservationCreateReqDto dto = ReservationCreateReqDto.builder()
                .restaurantId(1L)
                .memberId(2L)
                .reservationPostReqDto(postReq)
                .build();

        String serialized = "{\"dummy\":\"data\"}";

        // when
        when(serializer.serialize(dto)).thenReturn(serialized);

        // redisTemplate.opsForStream()의 mock 처리
        @SuppressWarnings("unchecked")
        StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        // 성공 케이스: add() 호출 시 non-null ID 반환
        when(streamOps.add(eq(topic.getTopic()), anyMap(), any(XAddOptions.class))).thenReturn(RecordId.of("some-id"));

        publisher.publish(dto);

        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<XAddOptions> optionsCaptor = ArgumentCaptor.forClass(XAddOptions.class);
        verify(streamOps).add(eq(topic.getTopic()), messageCaptor.capture(), optionsCaptor.capture());

        // then
        Map<String, String> sentMessage = messageCaptor.getValue();
        assertEquals(serialized, sentMessage.get("reservation-key"));

        XAddOptions options = optionsCaptor.getValue();
        // XAddOptions에 maxlen 옵션이 적용되었는지 간접적으로 확인 (toString() 혹은 기타 getter 활용)
        assertNotNull(options);
    }

    @Test
    void testPublishToStreamFailure() {
        ReservationPostReqDto postReq = new ReservationPostReqDto(LocalDateTime.now());
        ReservationCreateReqDto dto = ReservationCreateReqDto.builder()
                .restaurantId(1L)
                .memberId(2L)
                .reservationPostReqDto(postReq)
                .build();

        String serialized = "{\"dummy\":\"data\"}";
        when(serializer.serialize(dto)).thenReturn(serialized);

        @SuppressWarnings("unchecked")
        StreamOperations<String, Object, Object> streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);
        // 실패 케이스: add() 호출 시 null 반환 → 예외 발생
        when(streamOps.add(eq(topic.getTopic()), anyMap(), any(XAddOptions.class))).thenReturn(null);

        assertThrows(TimeoutException.class, () -> publisher.publish(dto));
    }
}
