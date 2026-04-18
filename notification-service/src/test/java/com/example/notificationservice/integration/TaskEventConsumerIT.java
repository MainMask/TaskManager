package com.example.notificationservice.integration;

import com.example.notificationservice.repository.NotificationRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestKafkaConfig.class)
class TaskEventConsumerIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("notificationdb_test")
            .withUsername("admin")
            .withPassword("admin");

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.7.0")
                    .asCompatibleSubstituteFor("apache/kafka"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationRecordRepository notificationRecordRepository;

    @Test
    void whenTaskCreatedEventReceived_thenNotificationRecordPersisted() {
        var payload = """
                {"eventType":"TASK_CREATED","taskId":42,"title":"Test Task",
                 "description":null,"status":"TODO","userId":1,
                 "categoryId":1,"categoryName":"Work",
                 "occurredAt":"2026-04-18T10:00:00Z"}
                """;

        kafkaTemplate.send("task-events", "42", payload);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var records = notificationRecordRepository.findByTaskId(42L);
            assertThat(records).hasSize(1);
            assertThat(records.get(0).getEventType()).isEqualTo("TASK_CREATED");
            assertThat(records.get(0).getTaskId()).isEqualTo(42L);
            assertThat(records.get(0).getDispatchStrategy()).isEqualTo("DATABASE");
        });
    }

    @Test
    void whenTaskDeletedEventReceived_thenNotificationRecordWithCorrectType() {
        var payload = """
                {"eventType":"TASK_DELETED","taskId":99,"title":"Deleted Task",
                 "occurredAt":"2026-04-18T11:00:00Z"}
                """;

        kafkaTemplate.send("task-events", "99", payload);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var records = notificationRecordRepository.findByTaskId(99L);
            assertThat(records).isNotEmpty();
            assertThat(records.get(0).getEventType()).isEqualTo("TASK_DELETED");
        });
    }
}
