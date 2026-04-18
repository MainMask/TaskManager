package com.example.taskservice.integration;

import com.example.taskservice.dto.TaskDtoGroup;
import com.example.taskservice.dto.UserDtoGroup;
import com.example.taskservice.model.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class TaskControllerIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("taskmanager_test")
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
        registry.add("app.cache.redis.enabled", () -> "false");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAllTasks_withPaginationParams_returnsOkWithPageStructure() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void createUser_and_createTask_succeeds() throws Exception {
        // Create a user
        var userCreate = new UserDtoGroup.Create("Alice IT", "alice.it." + System.currentTimeMillis() + "@test.com");
        var userResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreate)))
                .andExpect(status().isOk())
                .andReturn();

        var userBody = objectMapper.readTree(userResult.getResponse().getContentAsString());
        var userId = userBody.path("data").path("id").asLong();

        // Get first available category (auto-seeded)
        var catResult = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andReturn();

        var catBody = objectMapper.readTree(catResult.getResponse().getContentAsString());
        var categoryId = catBody.path("data").get(0).path("id").asLong();

        // Create a task
        var taskCreate = new TaskDtoGroup.Create("Integration Task", "desc", TaskStatus.TODO, userId, categoryId);
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Integration Task"))
                .andExpect(jsonPath("$.data.status").value("TODO"))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void getTaskById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTasks_filteredByStatus_returnsOk() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("status", "TODO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }
}
