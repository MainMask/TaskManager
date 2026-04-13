package com.example.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public final class UserDtoGroup {

    public record Response(
            Long id,
            String name,
            String email,
            LocalDateTime createdAt
    ) implements java.io.Serializable {}

    public record Create(
            @NotBlank(message = "Name cannot be blank") String name,
            @NotBlank(message = "Email cannot be blank") @Email(message = "Email should be valid") String email
    ) {}

    public record Update(
            @NotBlank(message = "Name cannot be blank") String name,
            @NotBlank(message = "Email cannot be blank") @Email(message = "Email should be valid") String email
    ) {}
}
