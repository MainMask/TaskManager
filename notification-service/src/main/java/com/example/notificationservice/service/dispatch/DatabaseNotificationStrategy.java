package com.example.notificationservice.service.dispatch;

import com.example.notificationservice.model.NotificationRecord;
import com.example.notificationservice.repository.NotificationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class DatabaseNotificationStrategy implements NotificationDispatchStrategy {

    private final NotificationRecordRepository repository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public void dispatch(Long taskId, String eventType, String payload) {
        var record = NotificationRecord.builder()
                .taskId(taskId)
                .eventType(eventType)
                .payload(payload)
                .dispatchStrategy(strategyName())
                .build();
        var saved = repository.save(record);
        log.info("Notification persisted: id={}, taskId={}, eventType={}", saved.getId(), taskId, eventType);
    }

    @Override
    public String strategyName() {
        return "DATABASE";
    }
}
