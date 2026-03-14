package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public final class CategoryDtoGroup {

    public record Response(Long id, String name) {}

    public record CreateOrUpdate(
            @NotBlank(message = "Name cannot be blank") String name
    ) {}
}
