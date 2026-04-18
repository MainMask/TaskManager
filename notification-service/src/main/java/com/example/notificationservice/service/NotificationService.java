package com.example.notificationservice.service;

import com.example.notificationservice.service.dispatch.NotificationDispatchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationDispatchStrategy dispatchStrategy;

    public void handle(Long taskId, String eventType, String payload) {
        log.debug("Handling event: type={}, taskId={}", eventType, taskId);
        dispatchStrategy.dispatch(taskId, eventType, payload);
    }
}
