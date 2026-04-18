package com.example.taskservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public final class CategoryDtoGroup {

    public record Response(
            Long id,
            String name
    ) implements Serializable {}

    public record CreateOrUpdate(
            @NotBlank(message = "Name cannot be blank") String name
    ) {}
}
