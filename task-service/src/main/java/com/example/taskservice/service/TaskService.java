package com.example.taskservice.service;

import com.example.taskservice.dto.TaskDtoGroup;
import com.example.taskservice.exception.ResourceNotFoundException;
import com.example.taskservice.mapper.TaskMapper;
import com.example.taskservice.model.Task;
import com.example.taskservice.model.TaskStatus;
import com.example.taskservice.producer.TaskEventProducer;
import com.example.taskservice.repository.TaskRepository;
import com.example.taskservice.repository.TaskSpecification;
import com.example.taskservice.service.priority.TaskPriorityStrategy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TaskMapper taskMapper;
    private final TaskEventProducer eventProducer;
    private final Map<String, TaskPriorityStrategy> strategies;

    @Value("${app.task.priority.strategy:statusBasedPriorityStrategy}")
    private String strategyName;

    private TaskPriorityStrategy priorityStrategy;

    public TaskService(
            TaskRepository taskRepository,
            UserService userService,
            CategoryService categoryService,
            TaskMapper taskMapper,
            TaskEventProducer eventProducer,
            Map<String, TaskPriorityStrategy> strategies) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.taskMapper = taskMapper;
        this.eventProducer = eventProducer;
        this.strategies = strategies;
    }

    @PostConstruct
    void init() {
        priorityStrategy = strategies.getOrDefault(strategyName,
                strategies.values().iterator().next());
        log.info("Active task priority strategy: {}", priorityStrategy.strategyName());
    }

    @Cacheable(value = "tasks", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #status + '-' + #from + '-' + #to")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<TaskDtoGroup.Response> getAllTasks(
            TaskStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        log.info("Fetching tasks | status={}, from={}, to={}, page={}", status, from, to, pageable.getPageNumber());
        var spec = TaskSpecification.withFilters(status, from, to);
        var taskPage = taskRepository.findAll(spec, pageable);
        log.debug("Found {} tasks (total={})", taskPage.getNumberOfElements(), taskPage.getTotalElements());
        return taskPage.map(taskMapper::toResponse);
    }

    @Cacheable(value = "tasks", key = "#id")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public TaskDtoGroup.Response getTaskById(Long id) {
        log.debug("Fetching task by id={}", id);
        return taskMapper.toResponse(getTaskEntity(id));
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TaskDtoGroup.Response createTask(TaskDtoGroup.Create dto) {
        log.info("Creating task | title={}, userId={}, categoryId={}", dto.title(), dto.userId(), dto.categoryId());
        var user = userService.getUserEntity(dto.userId());
        var category = categoryService.getCategoryEntity(dto.categoryId());

        var task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .status(dto.status())
                .user(user)
                .category(category)
                .build();
        var saved = taskRepository.save(task);

        var priority = priorityStrategy.calculatePriority(saved);
        log.info("Task created: id={}, priority={}({})", saved.getId(), priority, priorityStrategy.strategyName());

        eventProducer.sendTaskCreated(saved);
        return taskMapper.toResponse(saved);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public TaskDtoGroup.Response updateTask(Long id, TaskDtoGroup.Update dto) {
        log.info("Updating task id={}", id);
        var task = getTaskEntity(id);
        var previousStatus = task.getStatus();
        var category = categoryService.getCategoryEntity(dto.categoryId());

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setCategory(category);
        var saved = taskRepository.save(task);

        if (previousStatus != dto.status()) {
            eventProducer.sendTaskStatusChanged(id, previousStatus, dto.status());
        }
        eventProducer.sendTaskUpdated(saved);
        log.info("Task updated: id={}", saved.getId());
        return taskMapper.toResponse(saved);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        log.info("Deleting task id={}", id);
        var task = getTaskEntity(id);
        taskRepository.delete(task);
        eventProducer.sendTaskDeleted(id, task.getTitle());
        log.info("Task deleted: id={}", id);
    }

    public Task getTaskEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found: id={}", id);
                    return new ResourceNotFoundException("Task", id);
                });
    }
}
