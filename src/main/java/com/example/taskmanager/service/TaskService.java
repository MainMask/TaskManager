package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDtoGroup;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<TaskDtoGroup.Response> getAllTasks(Long categoryId, Long userId) {
        List<Task> tasks;
        if (categoryId != null && userId != null) {
            tasks = taskRepository.findByCategoryId(categoryId).stream()
                    .filter(t -> t.getUser().getId().equals(userId))
                    .toList();
        } else if (categoryId != null) {
            tasks = taskRepository.findByCategoryId(categoryId);
        } else if (userId != null) {
            tasks = taskRepository.findByUserId(userId);
        } else {
            tasks = taskRepository.findAll();
        }
        return tasks.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskDtoGroup.Response getTaskById(Long id) {
        Task task = getTaskEntity(id);
        return mapToResponse(task);
    }

    @Transactional
    public TaskDtoGroup.Response createTask(TaskDtoGroup.Create dto) {
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
        return mapToResponse(task);
    }

    @Transactional
    public TaskDtoGroup.Response updateTask(Long id, TaskDtoGroup.Update dto) {
        Task task = getTaskEntity(id);
        Category category = categoryService.getCategoryEntity(dto.categoryId());

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setStatus(dto.status());
        task.setCategory(category);
        taskRepository.save(task);
        return mapToResponse(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = getTaskEntity(id);
        taskRepository.delete(task);
    }

    private Task getTaskEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    private TaskDtoGroup.Response mapToResponse(Task task) {
        return new TaskDtoGroup.Response(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                userService.mapToResponse(task.getUser()),
                categoryService.mapToResponse(task.getCategory()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
