package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCategoryId(Long categoryId);
    List<Task> findByUserId(Long userId);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByCategoryIdAndStatus(Long categoryId, TaskStatus status);
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
}
