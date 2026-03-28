package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.TaskDtoGroup;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management API")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Get all tasks with optional filters")
    @GetMapping
    public ApiResponse<List<TaskDtoGroup.Response>> getAllTasks(
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by task status") @RequestParam(required = false) TaskStatus status
    ) {
        log.info("GET /api/tasks | categoryId={}, userId={}, status={}", categoryId, userId, status);
        return ApiResponse.success(taskService.getAllTasks(categoryId, userId, status));
    }

    @Operation(summary = "Get task by ID")
    @GetMapping("/{id}")
    public ApiResponse<TaskDtoGroup.Response> getTaskById(@PathVariable Long id) {
        log.info("GET /api/tasks/{}", id);
        return ApiResponse.success(taskService.getTaskById(id));
    }

    @Operation(summary = "Create task")
    @PostMapping
    public ApiResponse<TaskDtoGroup.Response> createTask(@Valid @RequestBody TaskDtoGroup.Create dto) {
        log.info("POST /api/tasks | title={}, userId={}", dto.title(), dto.userId());
        return ApiResponse.success(taskService.createTask(dto));
    }

    @Operation(summary = "Update task")
    @PutMapping("/{id}")
    public ApiResponse<TaskDtoGroup.Response> updateTask(
            @PathVariable Long id, @Valid @RequestBody TaskDtoGroup.Update dto) {
        log.info("PUT /api/tasks/{}", id);
        return ApiResponse.success(taskService.updateTask(id, dto));
    }

    @Operation(summary = "Delete task")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        log.info("DELETE /api/tasks/{}", id);
        taskService.deleteTask(id);
        return ApiResponse.success(null);
    }
}
