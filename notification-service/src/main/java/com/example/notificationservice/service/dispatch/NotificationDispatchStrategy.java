package com.example.notificationservice.service.dispatch;

public interface NotificationDispatchStrategy {

    void dispatch(Long taskId, String eventType, String payload);

    String strategyName();
}
