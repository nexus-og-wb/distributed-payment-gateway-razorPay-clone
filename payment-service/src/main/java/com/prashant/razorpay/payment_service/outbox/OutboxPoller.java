package com.prashant.razorpay.payment_service.outbox;


import com.prashant.razorpay.common_lib.config.KafkaProperties;
import com.prashant.razorpay.common_lib.enums.OutboxStatus;
import com.prashant.razorpay.payment_service.entity.OutboxEvent;
import com.prashant.razorpay.payment_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OutboxResultHandler outboxResultHandler;

    @Scheduled(fixedRate = 5000)
    @SchedulerLock(name = "payment-service-outbox-poller", lockAtMostFor = "1m", lockAtLeastFor = "1s")
    public void poll(){


        List<OutboxEvent> pendingEvents = outboxEventRepository
                    .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for(OutboxEvent event: pendingEvents){
            try {

                String topic = kafkaProperties.topicFor(event.getAggregateType());
                String key = extractMerchantId(event.getPayload());
                Map<String, Object> envelope = Map.of(
                        "eventType", event.getEventType(),
                        "aggregateType", event.getAggregateType(),
                        "aggregateId", event.getAggregateId(),
                        "data", event.getPayload()
                );

                kafkaTemplate.send(topic, key, envelope).get(5, TimeUnit.SECONDS);

                outboxResultHandler.handleEventPublished(event);


            }catch (Exception e) {
                    log.error("Outbox event failed, eventId: {}, attempts: {}", event.getId(), event.getAttempts());
                    outboxResultHandler.handleEventFailed(event, e.getMessage());
            }
        }

    }



    private String extractMerchantId(Map<String,Object> payload){
        Object value = payload.get("merchantId");
        return value != null ? value.toString() : "unknown";
    }
}
