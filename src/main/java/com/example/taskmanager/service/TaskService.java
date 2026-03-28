package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDtoGroup;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TaskMapper taskMapper;

    @Cacheable(value = "tasks")
    @Transactional(readOnly = true)
    public List<TaskDtoGroup.Response> getAllTasks(Long categoryId, Long userId, TaskStatus status) {
        log.info("Fetching tasks | categoryId={}, userId={}, status={}", categoryId, userId, status);
        List<Task> tasks;

        if (categoryId != null && userId != null && status != null) {
            tasks = taskRepository.findByCategoryIdAndStatus(categoryId, status).stream()
                    .filter(t -> t.getUser().getId().equals(userId))
                    .toList();
        } else if (categoryId != null && userId != null) {
            tasks = taskRepository.findByCategoryId(categoryId).stream()
                    .filter(t -> t.getUser().getId().equals(userId))
                    .toList();
        } else if (categoryId != null && status != null) {
            tasks = taskRepository.findByCategoryIdAndStatus(categoryId, status);
        } else if (userId != null && status != null) {
            tasks = taskRepository.findByUserIdAndStatus(userId, status);
        } else if (categoryId != null) {
            tasks = taskRepository.findByCategoryId(categoryId);
        } else if (userId != null) {
            tasks = taskRepository.findByUserId(userId);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }

        log.debug("Found {} tasks", tasks.size());
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    @Cacheable(value = "tasks", key = "#id")
    @Transactional(readOnly = true)
    public TaskDtoGroup.Response getTaskById(Long id) {
        log.debug("Fetching task by id={}", id);
        return taskMapper.toResponse(getTaskEntity(id));
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskDtoGroup.Response createTask(TaskDtoGroup.Create dto) {
        log.info("Creating task | title={}, userId={}, categoryId={}", dto.title(), dto.userId(), dto.categoryId());
        User user = userService.getUserEntity(dto.userId());
        Category category = categoryService.getCategoryEntity(dto.categoryId());

        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .status(dto.status())
                .user(user)
                .category(category)
                .build();
        taskRepository.save(task);
        log.info("Task created: id={}", task.getId());
        return taskMapper.toResponse(task);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public TaskDtoGroup.Response updateTask(Long id, TaskDtoGroup.Update dto) {
        log.info("Updating task id={}", id);
        Task task = getTaskEntity(id);
        Category category = categoryService.getCategoryEntity(dto.categoryId());

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setCategory(category);
        taskRepository.save(task);
        log.info("Task updated: id={}", task.getId());
        return taskMapper.toResponse(task);
    }

    @CacheEvict(value = "tasks", allEntries = true)
    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task id={}", id);
        Task task = getTaskEntity(id);
        taskRepository.delete(task);
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
