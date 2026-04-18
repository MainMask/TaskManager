package com.example.taskservice.mapper;

import com.example.taskservice.dto.CategoryDtoGroup;
import com.example.taskservice.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDtoGroup.Response toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryDtoGroup.CreateOrUpdate dto);
}
