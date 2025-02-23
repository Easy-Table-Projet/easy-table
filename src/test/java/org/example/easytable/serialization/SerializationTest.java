package org.example.easytable.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.easytable.reservation.dto.request.ReservationGetByRestaurantReqDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SerializationTest {
    private final RedisTemplate<String, ReservationGetByRestaurantReqDto> redisTemplate;

    @Autowired
    public SerializationTest(RedisTemplate<String, ReservationGetByRestaurantReqDto> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Test
    public void serializationTest() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // when
        ReservationGetByRestaurantReqDto original = new ReservationGetByRestaurantReqDto(1L, "test-id");
        String json = mapper.writeValueAsString(original);
        System.out.println("Serialized JSON: " + json);

        ReservationGetByRestaurantReqDto deserialized = mapper.readValue(json, ReservationGetByRestaurantReqDto.class);
        System.out.println("Deserialized object: " + deserialized);

        // then
        assertEquals(original.getRequestId(), deserialized.getRequestId());
    }

    @Test
    public void redisSerializationTest() throws Exception {
        // given
        String redisKey = "test:reservation:req";

        String originalRequestId = UUID.randomUUID().toString();
        ReservationGetByRestaurantReqDto originalObject =
            new ReservationGetByRestaurantReqDto(1L, originalRequestId);

        // when
        redisTemplate.opsForSet().add(redisKey, originalObject);

        ReservationGetByRestaurantReqDto retrievedObject = redisTemplate.opsForSet().pop(redisKey);

        // then
        assertNotNull(retrievedObject);
        assertEquals(originalObject.getRestaurantId(), retrievedObject.getRestaurantId());
        assertEquals(originalObject.getRequestId(), retrievedObject.getRequestId());
    }
}
