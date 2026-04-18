package com.example.notificationservice.service.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogNotificationStrategy implements NotificationDispatchStrategy {

    @Override
    public void dispatch(Long taskId, String eventType, String payload) {
        log.info("[NOTIFICATION] eventType={} taskId={} payload={}", eventType, taskId, payload);
    }

    @Override
    public String strategyName() {
        return "LOG";
    }
}
