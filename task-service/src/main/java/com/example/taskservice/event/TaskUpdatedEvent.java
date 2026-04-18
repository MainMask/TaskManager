package com.example.taskservice.event;

import java.time.Instant;

public record TaskUpdatedEvent(
        Long taskId,
        String title,
        String description,
        String status,
        Long categoryId,
        String categoryName,
        Instant occurredAt
) {
    @com.fasterxml.jackson.annotation.JsonProperty("eventType")
    public String eventType() {
        return "TASK_UPDATED";
    }
}
