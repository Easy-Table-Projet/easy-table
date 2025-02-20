package org.example.easytable.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.easytable.reservation.dto.request.ReservationGetByRestaurantReqDtoImpl;
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
    private final RedisTemplate<String, ReservationGetByRestaurantReqDtoImpl> redisTemplate;

    @Autowired
    public SerializationTest(RedisTemplate<String, ReservationGetByRestaurantReqDtoImpl> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Test
    public void serializationTest() throws Exception {
        // given
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // when
        ReservationGetByRestaurantReqDtoImpl original = new ReservationGetByRestaurantReqDtoImpl(1L, "test-id");
        String json = mapper.writeValueAsString(original);
        System.out.println("Serialized JSON: " + json);

        ReservationGetByRestaurantReqDtoImpl deserialized = mapper.readValue(json, ReservationGetByRestaurantReqDtoImpl.class);
        System.out.println("Deserialized object: " + deserialized);

        // then
        assertEquals(original.getRequestId(), deserialized.getRequestId());
    }

    @Test
    public void redisSerializationTest() throws Exception {
        // given
        String redisKey = "test:reservation:req";

        String originalRequestId = UUID.randomUUID().toString();
        ReservationGetByRestaurantReqDtoImpl originalObject =
            new ReservationGetByRestaurantReqDtoImpl(1L, originalRequestId);

        // when
        redisTemplate.opsForSet().add(redisKey, originalObject);

        ReservationGetByRestaurantReqDtoImpl retrievedObject = redisTemplate.opsForSet().pop(redisKey);

        // then
        assertNotNull(retrievedObject);
        assertEquals(originalObject.getRestaurantId(), retrievedObject.getRestaurantId());
        assertEquals(originalObject.getRequestId(), retrievedObject.getRequestId());
    }
}
