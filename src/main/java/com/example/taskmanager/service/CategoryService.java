package com.example.taskmanager.service;

import com.example.taskmanager.dto.CategoryDtoGroup;
import com.example.taskmanager.exception.BusinessException;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.mapper.CategoryMapper;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @PostConstruct
    public void initDefaultCategories() {
        log.info("Initializing default categories");
        List<String> defaults = List.of("Home", "Work", "Education", "Health", "Other");
        for (String name : defaults) {
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(Category.builder().name(name).build());
                log.debug("Created default category: {}", name);
            }
        }
    }

    @Cacheable("categories")
    @Transactional(readOnly = true)
    public List<CategoryDtoGroup.Response> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "categories", key = "#id")
    @Transactional(readOnly = true)
    public CategoryDtoGroup.Response getCategoryById(Long id) {
        log.debug("Fetching category by id={}", id);
        return categoryMapper.toResponse(getCategoryEntity(id));
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryDtoGroup.Response createCategory(CategoryDtoGroup.CreateOrUpdate dto) {
        log.info("Creating category | name={}", dto.name());
        if (categoryRepository.existsByName(dto.name())) {
            log.warn("Duplicate category name: {}", dto.name());
            throw new BusinessException("Category with this name already exists");
        }
        Category category = categoryMapper.toEntity(dto);
        categoryRepository.save(category);
        log.info("Category created: id={}", category.getId());
        return categoryMapper.toResponse(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public CategoryDtoGroup.Response updateCategory(Long id, CategoryDtoGroup.CreateOrUpdate dto) {
        log.info("Updating category id={}", id);
        Category category = getCategoryEntity(id);
        if (!category.getName().equals(dto.name()) && categoryRepository.existsByName(dto.name())) {
            log.warn("Duplicate category name on update: {}", dto.name());
            throw new BusinessException("Category with this name already exists");
        }
        category.setName(dto.name());
        categoryRepository.save(category);
        log.info("Category updated: id={}", category.getId());
        return categoryMapper.toResponse(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category id={}", id);
        Category category = getCategoryEntity(id);
        categoryRepository.delete(category);
        log.info("Category deleted: id={}", id);
    }

    public Category getCategoryEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found: id={}", id);
                    return new ResourceNotFoundException("Category", id);
                });
    }

    // Метод оставлен для использования в TaskService
    public CategoryDtoGroup.Response mapToResponse(Category category) {
        return categoryMapper.toResponse(category);
    }
}
