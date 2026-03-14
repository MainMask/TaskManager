package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.CategoryDtoGroup;
import com.example.taskmanager.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management API")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all categories")
    @GetMapping
    public ApiResponse<List<CategoryDtoGroup.Response>> getAllCategories() {
        return ApiResponse.success(categoryService.getAllCategories());
    }

    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ApiResponse<CategoryDtoGroup.Response> getCategoryById(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Create category")
    @PostMapping
    public ApiResponse<CategoryDtoGroup.Response> createCategory(@Valid @RequestBody CategoryDtoGroup.CreateOrUpdate dto) {
        return ApiResponse.success(categoryService.createCategory(dto));
    }

    @Operation(summary = "Update category")
    @PutMapping("/{id}")
    public ApiResponse<CategoryDtoGroup.Response> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDtoGroup.CreateOrUpdate dto) {
        return ApiResponse.success(categoryService.updateCategory(id, dto));
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }
}
