package com.example.taskservice.mapper;

import com.example.taskservice.dto.UserDtoGroup;
import com.example.taskservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDtoGroup.Response toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserDtoGroup.Create dto);
}
