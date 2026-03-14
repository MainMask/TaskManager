package com.example.taskmanager.service;

import com.example.taskmanager.dto.CategoryDtoGroup;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void initDefaultCategories() {
        List<String> defaults = List.of("Home", "Work", "Education", "Health", "Other");
        for (String name : defaults) {
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(Category.builder().name(name).build());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryDtoGroup.Response> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDtoGroup.Response getCategoryById(Long id) {
        Category category = getCategoryEntity(id);
        return mapToResponse(category);
    }

    @Transactional
    public CategoryDtoGroup.Response createCategory(CategoryDtoGroup.CreateOrUpdate dto) {
        if (categoryRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        Category category = Category.builder()
                .name(dto.name())
                .build();
        categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional
    public CategoryDtoGroup.Response updateCategory(Long id, CategoryDtoGroup.CreateOrUpdate dto) {
        Category category = getCategoryEntity(id);
        if (!category.getName().equals(dto.name()) && categoryRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        category.setName(dto.name());
        categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryEntity(id);
        categoryRepository.delete(category);
    }

    public Category getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    }

    public CategoryDtoGroup.Response mapToResponse(Category category) {
        return new CategoryDtoGroup.Response(category.getId(), category.getName());
    }
}
