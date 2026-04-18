package com.example.taskservice.service;

import com.example.taskservice.dto.TaskDtoGroup;
import com.example.taskservice.dto.UserDtoGroup;
import com.example.taskservice.dto.CategoryDtoGroup;
import com.example.taskservice.exception.ResourceNotFoundException;
import com.example.taskservice.mapper.TaskMapper;
import com.example.taskservice.model.Category;
import com.example.taskservice.model.Task;
import com.example.taskservice.model.TaskStatus;
import com.example.taskservice.model.User;
import com.example.taskservice.producer.TaskEventProducer;
import com.example.taskservice.repository.TaskRepository;
import com.example.taskservice.service.priority.StatusBasedPriorityStrategy;
import com.example.taskservice.service.priority.TaskPriorityStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserService userService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskEventProducer eventProducer;

    private TaskService taskService;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        TaskPriorityStrategy strategy = new StatusBasedPriorityStrategy();
        Map<String, TaskPriorityStrategy> strategies = Map.of("statusBasedPriorityStrategy", strategy);
        taskService = new TaskService(taskRepository, userService, categoryService,
                taskMapper, eventProducer, strategies);
        ReflectionTestUtils.setField(taskService, "strategyName", "statusBasedPriorityStrategy");
        taskService.init();

        user = User.builder().id(1L).name("Alice").email("alice@test.com")
                .createdAt(LocalDateTime.now()).build();
        category = Category.builder().id(2L).name("Work").build();
    }

    @Test
    void createTask_success_savesAndPublishesEvent() {
        var dto = new TaskDtoGroup.Create("Buy milk", null, TaskStatus.TODO, 1L, 2L);
        var savedTask = Task.builder()
                .id(10L).title("Buy milk").status(TaskStatus.TODO)
                .user(user).category(category).createdAt(LocalDateTime.now()).build();
        var expectedResponse = new TaskDtoGroup.Response(
                10L, "Buy milk", null, TaskStatus.TODO,
                new UserDtoGroup.Response(1L, "Alice", "alice@test.com", user.getCreatedAt()),
                new CategoryDtoGroup.Response(2L, "Work"),
                savedTask.getCreatedAt(), null);

        given(userService.getUserEntity(1L)).willReturn(user);
        given(categoryService.getCategoryEntity(2L)).willReturn(category);
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);
        given(taskMapper.toResponse(savedTask)).willReturn(expectedResponse);

        var result = taskService.createTask(dto);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("Buy milk");
        then(taskRepository).should().save(any(Task.class));
        then(eventProducer).should().sendTaskCreated(savedTask);
    }

    @Test
    void getTaskById_notFound_throwsResourceNotFoundException() {
        given(taskRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteTask_success_deletesAndPublishesDeletedEvent() {
        var task = Task.builder().id(5L).title("Old task").status(TaskStatus.DONE)
                .user(user).category(category).createdAt(LocalDateTime.now()).build();
        given(taskRepository.findById(5L)).willReturn(Optional.of(task));

        taskService.deleteTask(5L);

        then(taskRepository).should().delete(task);
        then(eventProducer).should().sendTaskDeleted(5L, "Old task");
    }

    @Test
    void updateTask_statusChanged_publishesStatusChangedEvent() {
        var existing = Task.builder().id(3L).title("Old").status(TaskStatus.TODO)
                .user(user).category(category).createdAt(LocalDateTime.now()).build();
        var dto = new TaskDtoGroup.Update("New Title", null, TaskStatus.IN_PROGRESS, 2L);
        var savedTask = Task.builder().id(3L).title("New Title").status(TaskStatus.IN_PROGRESS)
                .user(user).category(category).createdAt(existing.getCreatedAt()).build();

        given(taskRepository.findById(3L)).willReturn(Optional.of(existing));
        given(categoryService.getCategoryEntity(2L)).willReturn(category);
        given(taskRepository.save(any())).willReturn(savedTask);
        given(taskMapper.toResponse(any())).willReturn(
                new TaskDtoGroup.Response(3L, "New Title", null, TaskStatus.IN_PROGRESS, null, null, null, null));

        taskService.updateTask(3L, dto);

        then(eventProducer).should().sendTaskStatusChanged(3L, TaskStatus.TODO, TaskStatus.IN_PROGRESS);
        then(eventProducer).should().sendTaskUpdated(savedTask);
    }

    @Test
    void updateTask_statusUnchanged_doesNotPublishStatusChangedEvent() {
        var existing = Task.builder().id(4L).title("Same").status(TaskStatus.IN_PROGRESS)
                .user(user).category(category).createdAt(LocalDateTime.now()).build();
        var dto = new TaskDtoGroup.Update("Same Updated", null, TaskStatus.IN_PROGRESS, 2L);
        var savedTask = Task.builder().id(4L).title("Same Updated").status(TaskStatus.IN_PROGRESS)
                .user(user).category(category).createdAt(existing.getCreatedAt()).build();

        given(taskRepository.findById(4L)).willReturn(Optional.of(existing));
        given(categoryService.getCategoryEntity(2L)).willReturn(category);
        given(taskRepository.save(any())).willReturn(savedTask);
        given(taskMapper.toResponse(any())).willReturn(
                new TaskDtoGroup.Response(4L, "Same Updated", null, TaskStatus.IN_PROGRESS, null, null, null, null));

        taskService.updateTask(4L, dto);

        then(eventProducer).should(org.mockito.BDDMockito.never())
                .sendTaskStatusChanged(any(), any(), any());
    }

    @Test
    void getAllTasks_withPagination_returnsPagedResult() {
        var pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        var task = Task.builder().id(1L).title("T1").status(TaskStatus.TODO)
                .user(user).category(category).createdAt(LocalDateTime.now()).build();
        var page = new PageImpl<>(List.of(task), pageable, 1L);

        given(taskRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable))).willReturn(page);
        given(taskMapper.toResponse(any())).willReturn(
                new TaskDtoGroup.Response(1L, "T1", null, TaskStatus.TODO, null, null, null, null));

        var result = taskService.getAllTasks(null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    }
}
