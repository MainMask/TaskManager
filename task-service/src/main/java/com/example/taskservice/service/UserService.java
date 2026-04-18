package com.example.taskservice.service;

import com.example.taskservice.dto.UserDtoGroup;
import com.example.taskservice.exception.BusinessException;
import com.example.taskservice.exception.ResourceNotFoundException;
import com.example.taskservice.mapper.UserMapper;
import com.example.taskservice.model.User;
import com.example.taskservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Cacheable("users")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<UserDtoGroup.Response> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public UserDtoGroup.Response getUserById(Long id) {
        log.debug("Fetching user by id={}", id);
        return userMapper.toResponse(getUserEntity(id));
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public UserDtoGroup.Response createUser(UserDtoGroup.Create dto) {
        log.info("Creating user | email={}", dto.email());
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Duplicate email attempt: {}", dto.email());
            throw new BusinessException("User with this email already exists");
        }
        var user = userMapper.toEntity(dto);
        userRepository.save(user);
        log.info("User created: id={}", user.getId());
        return userMapper.toResponse(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public UserDtoGroup.Response updateUser(Long id, UserDtoGroup.Update dto) {
        log.info("Updating user id={}", id);
        var user = getUserEntity(id);
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            log.warn("Duplicate email on update: {}", dto.email());
            throw new BusinessException("User with this email already exists");
        }
        user.setName(dto.name());
        user.setEmail(dto.email());
        userRepository.save(user);
        log.info("User updated: id={}", user.getId());
        return userMapper.toResponse(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        log.info("Deleting user id={}", id);
        var user = getUserEntity(id);
        userRepository.delete(user);
        log.info("User deleted: id={}", id);
    }

    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: id={}", id);
                    return new ResourceNotFoundException("User", id);
                });
    }

    public UserDtoGroup.Response mapToResponse(User user) {
        return userMapper.toResponse(user);
    }
}
