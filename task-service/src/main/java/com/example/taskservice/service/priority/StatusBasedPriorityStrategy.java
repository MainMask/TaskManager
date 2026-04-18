package com.example.taskservice.service.priority;

import com.example.taskservice.model.Task;
import org.springframework.stereotype.Component;

@Component("statusBasedPriorityStrategy")
public class StatusBasedPriorityStrategy implements TaskPriorityStrategy {

    @Override
    public int calculatePriority(Task task) {
        return switch (task.getStatus()) {
            case TODO        -> 10;
            case IN_PROGRESS -> 20;
            case DONE        -> 0;
        };
    }

    @Override
    public String strategyName() {
        return "STATUS_BASED";
    }
}
