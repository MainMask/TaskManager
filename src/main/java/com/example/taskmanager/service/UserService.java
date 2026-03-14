package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDtoGroup;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDtoGroup.Response> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDtoGroup.Response getUserById(Long id) {
        User user = getUserEntity(id);
        return mapToResponse(user);
    }

    @Transactional
    public UserDtoGroup.Response createUser(UserDtoGroup.Create dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .build();
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public UserDtoGroup.Response updateUser(Long id, UserDtoGroup.Update dto) {
        User user = getUserEntity(id);
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        user.setName(dto.name());
        user.setEmail(dto.email());
        userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntity(id);
        userRepository.delete(user);
    }

    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public UserDtoGroup.Response mapToResponse(User user) {
        return new UserDtoGroup.Response(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
