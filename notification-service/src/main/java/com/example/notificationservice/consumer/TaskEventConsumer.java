package com.example.notificationservice.consumer;

import com.example.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "task-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        log.info("Received Kafka message: topic={} partition={} offset={}",
                record.topic(), record.partition(), record.offset());
        try {
            var payload = record.value();
            var root = objectMapper.readValue(payload, JsonNode.class);
            var eventType = root.path("eventType").asText("UNKNOWN");
            var taskId = root.path("taskId").asLong(0L);
            notificationService.handle(taskId, eventType, payload);
        } catch (Exception ex) {
            log.error("Failed to process Kafka message: {}", ex.getMessage(), ex);
        }
    }
}
