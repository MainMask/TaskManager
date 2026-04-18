package com.example.taskservice.event;

import java.time.Instant;

public record TaskStatusChangedEvent(
        Long taskId,
        String previousStatus,
        String newStatus,
        Instant occurredAt
) {
    @com.fasterxml.jackson.annotation.JsonProperty("eventType")
    public String eventType() {
        return "TASK_STATUS_CHANGED";
    }
}
