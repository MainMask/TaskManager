package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.UserDtoGroup;
import com.example.taskmanager.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDtoGroup.Response toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserDtoGroup.Create dto);
}
