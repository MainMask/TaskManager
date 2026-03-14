package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.UserDtoGroup;
import com.example.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @GetMapping
    public ApiResponse<List<UserDtoGroup.Response>> getAllUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ApiResponse<UserDtoGroup.Response> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @Operation(summary = "Create user")
    @PostMapping
    public ApiResponse<UserDtoGroup.Response> createUser(@Valid @RequestBody UserDtoGroup.Create dto) {
        return ApiResponse.success(userService.createUser(dto));
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public ApiResponse<UserDtoGroup.Response> updateUser(@PathVariable Long id, @Valid @RequestBody UserDtoGroup.Update dto) {
        return ApiResponse.success(userService.updateUser(id, dto));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }
}
