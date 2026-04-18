package com.example.taskservice.service.priority;

import com.example.taskservice.model.Task;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component("agePriorityStrategy")
public class AgePriorityStrategy implements TaskPriorityStrategy {

    @Override
    public int calculatePriority(Task task) {
        var age = Duration.between(task.getCreatedAt(), LocalDateTime.now()).toDays();
        return (int) Math.min(age * 2, 100);
    }

    @Override
    public String strategyName() {
        return "AGE_BASED";
    }
}
