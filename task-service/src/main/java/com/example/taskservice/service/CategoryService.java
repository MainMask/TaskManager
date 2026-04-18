package com.example.taskservice.service;

import com.example.taskservice.dto.CategoryDtoGroup;
import com.example.taskservice.exception.BusinessException;
import com.example.taskservice.exception.ResourceNotFoundException;
import com.example.taskservice.mapper.CategoryMapper;
import com.example.taskservice.model.Category;
import com.example.taskservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void initDefaultCategories() {
        log.info("Initializing default categories");
        var defaults = List.of("Home", "Work", "Education", "Health", "Other");
        for (var name : defaults) {
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(Category.builder().name(name).build());
                log.debug("Created default category: {}", name);
            }
        }
    }

    @Cacheable("categories")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<CategoryDtoGroup.Response> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "categories", key = "#id")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public CategoryDtoGroup.Response getCategoryById(Long id) {
        log.debug("Fetching category by id={}", id);
        return categoryMapper.toResponse(getCategoryEntity(id));
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public CategoryDtoGroup.Response createCategory(CategoryDtoGroup.CreateOrUpdate dto) {
        log.info("Creating category | name={}", dto.name());
        if (categoryRepository.existsByName(dto.name())) {
            log.warn("Duplicate category name: {}", dto.name());
            throw new BusinessException("Category with this name already exists");
        }
        var category = categoryMapper.toEntity(dto);
        categoryRepository.save(category);
        log.info("Category created: id={}", category.getId());
        return categoryMapper.toResponse(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public CategoryDtoGroup.Response updateCategory(Long id, CategoryDtoGroup.CreateOrUpdate dto) {
        log.info("Updating category id={}", id);
        var category = getCategoryEntity(id);
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
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        log.info("Deleting category id={}", id);
        var category = getCategoryEntity(id);
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

    public CategoryDtoGroup.Response mapToResponse(Category category) {
        return categoryMapper.toResponse(category);
    }
}
