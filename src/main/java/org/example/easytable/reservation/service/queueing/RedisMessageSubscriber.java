package org.example.easytable.reservation.service.queueing;

import org.example.easytable.common.utils.SerializerUtil;
import org.example.easytable.reservation.dto.request.ReservationCreateReqDto;
import org.example.easytable.reservation.dto.response.ReservationCreateResDto;
import org.example.easytable.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageSubscriber implements MessageListener {
    private final SinksRegistry sinkRegistry;
    private final TaskExecutor taskExecutor;
    private final SerializerUtil<ReservationCreateReqDto> serializerUtil;
    private final ReservationService reservationService;

    public RedisMessageSubscriber(
            SinksRegistry sinkRegistry,
            @Qualifier("subscriberExecutor") TaskExecutor taskExecutor,
            SerializerUtil<ReservationCreateReqDto> serializerUtil,
            ReservationService reservationService
    ) {
        this.sinkRegistry = sinkRegistry;
        this.taskExecutor = taskExecutor;
        this.serializerUtil = serializerUtil;
        this.reservationService = reservationService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        ReservationCreateReqDto request = serializerUtil.deserialize(message.getBody());

        // TaskExecutor를 통해 별도 스레드에서 요청 처리
        taskExecutor.execute(() -> {
            ReservationCreateResDto response = reservationService.createReservation(request);

            // SinkRegistry를 통해 요청을 보낸 스레드에 결과 전송
            sinkRegistry.completeSink(request.getRequestId(), response);
        });
    }
}
