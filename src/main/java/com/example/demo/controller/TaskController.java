package com.example.demo.controller;

import com.example.demo.model.TaskProgressDTO;
import com.example.demo.model.TasksDTO;
import com.example.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<UUID> createTask(@RequestBody TasksDTO tasksDTO) {
        UUID taskId = taskService.createTask(tasksDTO);
        return ResponseEntity.ok(taskId);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskProgressDTO> getTaskProgress(@PathVariable UUID taskId) {
        TaskProgressDTO progress = taskService.getTaskProgress(taskId);
        return ResponseEntity.ok(progress);
    }
}
