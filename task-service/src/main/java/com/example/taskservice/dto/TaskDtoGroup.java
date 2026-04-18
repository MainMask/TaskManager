package com.example.taskservice.dto;

import com.example.taskservice.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;

public final class TaskDtoGroup {

    public record Response(
            Long id,
            String title,
            String description,
            TaskStatus status,
            UserDtoGroup.Response user,
            CategoryDtoGroup.Response category,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) implements Serializable {}

    public record Create(
            @NotBlank(message = "Title cannot be blank") String title,
            String description,
            @NotNull(message = "Status cannot be null") TaskStatus status,
            @NotNull(message = "UserId cannot be null") Long userId,
            @NotNull(message = "CategoryId cannot be null") Long categoryId
    ) {}

    public record Update(
            @NotBlank(message = "Title cannot be blank") String title,
            String description,
            @NotNull(message = "Status cannot be null") TaskStatus status,
            @NotNull(message = "CategoryId cannot be null") Long categoryId
    ) {}
}
