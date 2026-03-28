package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.CategoryDtoGroup;
import com.example.taskmanager.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDtoGroup.Response toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryDtoGroup.CreateOrUpdate dto);
}
