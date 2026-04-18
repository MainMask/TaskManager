package com.example.taskservice.service.priority;

import com.example.taskservice.model.Task;

public interface TaskPriorityStrategy {

    int calculatePriority(Task task);

    String strategyName();
}
