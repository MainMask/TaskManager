package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public final class CategoryDtoGroup {

    public record Response(Long id, String name) implements java.io.Serializable {}

    public record CreateOrUpdate(
            @NotBlank(message = "Name cannot be blank") String name
    ) {}
}
