package com.example.taskservice.repository;

import com.example.taskservice.model.Task;
import com.example.taskservice.model.TaskStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class TaskSpecification {

    private TaskSpecification() {}

    public static Specification<Task> withFilters(TaskStatus status, LocalDateTime from, LocalDateTime to) {
        return Specification
                .where(hasStatus(status))
                .and(createdAfter(from))
                .and(createdBefore(to))
                .and(fetchAssociations());
    }

    private static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Task> createdAfter(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    private static Specification<Task> createdBefore(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    private static Specification<Task> fetchAssociations() {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType()) {
                root.fetch("user", JoinType.INNER);
                root.fetch("category", JoinType.INNER);
            }
            return null;
        };
    }
}
