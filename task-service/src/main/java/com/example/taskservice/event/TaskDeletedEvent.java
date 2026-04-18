package com.example.taskservice.event;

import java.time.Instant;

public record TaskDeletedEvent(
        Long taskId,
        String title,
        Instant occurredAt
) {
    @com.fasterxml.jackson.annotation.JsonProperty("eventType")
    public String eventType() {
        return "TASK_DELETED";
    }
}
