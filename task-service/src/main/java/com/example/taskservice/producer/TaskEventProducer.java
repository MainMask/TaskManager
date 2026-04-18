package com.example.taskservice.producer;

import com.example.taskservice.event.TaskCreatedEvent;
import com.example.taskservice.event.TaskDeletedEvent;
import com.example.taskservice.event.TaskStatusChangedEvent;
import com.example.taskservice.event.TaskUpdatedEvent;
import com.example.taskservice.model.Task;
import com.example.taskservice.model.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventProducer {

    static final String TOPIC = "task-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTaskCreated(Task task) {
        var event = new TaskCreatedEvent(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getUser().getId(),
                task.getCategory().getId(),
                task.getCategory().getName(),
                Instant.now()
        );
        send(task.getId(), event);
    }

    public void sendTaskUpdated(Task task) {
        var event = new TaskUpdatedEvent(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getCategory().getId(),
                task.getCategory().getName(),
                Instant.now()
        );
        send(task.getId(), event);
    }

    public void sendTaskDeleted(Long taskId, String title) {
        var event = new TaskDeletedEvent(taskId, title, Instant.now());
        send(taskId, event);
    }

    public void sendTaskStatusChanged(Long taskId, TaskStatus previous, TaskStatus next) {
        var event = new TaskStatusChangedEvent(taskId, previous.name(), next.name(), Instant.now());
        send(taskId, event);
    }

    private void send(Long taskId, Object event) {
        var future = kafkaTemplate.send(TOPIC, taskId.toString(), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send Kafka event for taskId={}: {}", taskId, ex.getMessage(), ex);
            } else {
                log.info("Kafka event sent | topic={} partition={} offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
