package org.example.easytable.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class SerializationTest {
    private final SerializerUtil<ReservationCreateReqDto> serializer = new SerializerUtil<>(ReservationCreateReqDto.class);

    @Test
    public void testSerialize() {
        ReservationCreateReqDto request = new ReservationCreateReqDto(1L, 2L, null);

        String json = serializer.serialize(request);
        assertNotNull(json, "직렬화된 JSON 문자열은 null이어선 안됩니다.");

        // 간단히 JSON 문자열에 주요 필드가 포함되었는지 확인합니다.
        assertTrue(json.contains("\"restaurantId\""), "JSON에 restaurantId 키가 포함되어야 합니다.");
        assertTrue(json.contains("\"memberId\""), "JSON에 memberId 키가 포함되어야 합니다.");
    }

    @Test
    public void testDeserialize() {
        // JSON 문자열을 미리 정의합니다.
        String json = "{\"requestId\":\"abc123\",\"restaurantId\":1,\"memberId\":2,\"reservationPostReqDto\":null}";

        ReservationCreateReqDto request = serializer.deserialize(json);
        assertNotNull(request, "역직렬화된 객체는 null이면 안됩니다.");
        assertEquals("abc123", request.getRequestId(), "requestId가 올바르게 역직렬화되어야 합니다.");
        assertEquals(1L, request.getRestaurantId(), "restaurantId가 올바르게 역직렬화되어야 합니다.");
        assertEquals(2L, request.getMemberId(), "memberId가 올바르게 역직렬화되어야 합니다.");
        assertNull(request.getReservationPostReqDto(), "reservationPostReqDto는 null이어야 합니다.");
    }

    @Test
    public void testSerializeAndDeserialize() throws JsonProcessingException {
        // 원본 객체 생성
        ReservationCreateReqDto original = new ReservationCreateReqDto(1L, 2L, null);
        // (만약 requestId가 내부에서 자동 생성된다면, 직렬화/역직렬화 과정에서 동일하게 유지되는지 확인합니다.)

        String json = serializer.serialize(original);
        ReservationCreateReqDto deserialized = serializer.deserialize(json);

        // 각 필드 값이 동일한지 검증합니다.
        assertEquals(original.getRestaurantId(), deserialized.getRestaurantId(), "restaurantId가 동일해야 합니다.");
        assertEquals(original.getMemberId(), deserialized.getMemberId(), "memberId가 동일해야 합니다.");
        assertEquals(original.getReservationPostReqDto(), deserialized.getReservationPostReqDto(), "reservationPostReqDto가 동일해야 합니다.");
        assertEquals(original.getRequestId(), deserialized.getRequestId(), "requestId가 동일해야 합니다.");

        System.out.println("original requestId: " + original.getRequestId() + ", deserialized requestId: " + deserialized.getRequestId());
    }
}
