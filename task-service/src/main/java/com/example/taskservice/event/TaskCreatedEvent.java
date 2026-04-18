package com.example.taskservice.event;

import java.time.Instant;

public record TaskCreatedEvent(
        Long taskId,
        String title,
        String description,
        String status,
        Long userId,
        Long categoryId,
        String categoryName,
        Instant occurredAt
) {
    @com.fasterxml.jackson.annotation.JsonProperty("eventType")
    public String eventType() {
        return "TASK_CREATED";
    }
}
