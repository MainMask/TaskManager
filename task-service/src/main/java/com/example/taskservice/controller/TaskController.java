package com.example.taskservice.controller;

import com.example.taskservice.dto.ApiResponse;
import com.example.taskservice.dto.TaskDtoGroup;
import com.example.taskservice.model.TaskStatus;
import com.example.taskservice.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management API")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Get all tasks with optional filters, pagination and sorting")
    @GetMapping
    public ApiResponse<Page<TaskDtoGroup.Response>> getAllTasks(
            @Parameter(description = "Filter by task status") @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter from date (ISO-8601)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Filter to date (ISO-8601)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/tasks | status={}, from={}, to={}, page={}", status, from, to, pageable.getPageNumber());
        return ApiResponse.success(taskService.getAllTasks(status, from, to, pageable));
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
