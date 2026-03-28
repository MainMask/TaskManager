package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.TaskDtoGroup;
import com.example.taskmanager.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface TaskMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "category", source = "category")
    TaskDtoGroup.Response toResponse(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskDtoGroup.Create dto);
}
