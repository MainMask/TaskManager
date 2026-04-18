package com.example.taskservice.repository;

import com.example.taskservice.model.Task;
import com.example.taskservice.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    // Tasks in a category with user info eagerly loaded, optionally filtered by status
    @Query("""
            SELECT t FROM Task t
            JOIN FETCH t.user u
            WHERE t.category.id = :categoryId
              AND (:status IS NULL OR t.status = :status)
            ORDER BY t.createdAt DESC
            """)
    List<Task> findByCategoryWithUser(
            @Param("categoryId") Long categoryId,
            @Param("status") TaskStatus status);
}
