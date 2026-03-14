package com.example.taskmanager.dto;

public record ApiResponse<T>(T data, String message) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, "Success");
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, message);
    }
}
