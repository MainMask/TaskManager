package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.UserDtoGroup;
import com.example.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @GetMapping
    public ApiResponse<List<UserDtoGroup.Response>> getAllUsers() {
        log.info("GET /api/users");
        return ApiResponse.success(userService.getAllUsers());
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ApiResponse<UserDtoGroup.Response> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{}", id);
        return ApiResponse.success(userService.getUserById(id));
    }

    @Operation(summary = "Create user")
    @PostMapping
    public ApiResponse<UserDtoGroup.Response> createUser(@Valid @RequestBody UserDtoGroup.Create dto) {
        log.info("POST /api/users | email={}", dto.email());
        return ApiResponse.success(userService.createUser(dto));
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public ApiResponse<UserDtoGroup.Response> updateUser(
            @PathVariable Long id, @Valid @RequestBody UserDtoGroup.Update dto) {
        log.info("PUT /api/users/{}", id);
        return ApiResponse.success(userService.updateUser(id, dto));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}", id);
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }
}
