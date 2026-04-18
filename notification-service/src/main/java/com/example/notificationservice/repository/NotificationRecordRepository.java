package com.example.notificationservice.repository;

import com.example.notificationservice.model.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, Long> {
    List<NotificationRecord> findByTaskId(Long taskId);
    List<NotificationRecord> findByEventType(String eventType);
}
