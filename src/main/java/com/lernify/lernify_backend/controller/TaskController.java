package com.lernify.lernify_backend.controller;

import com.lernify.lernify_backend.model.Task;
import com.lernify.lernify_backend.model.User;
import com.lernify.lernify_backend.repository.TaskRepository;
import com.lernify.lernify_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /** GET all tasks for current user */
    @GetMapping
    public List<Task> getTasks() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username).orElseThrow();
        return taskRepository.findByUser(user);
    }

    /** CREATE new task */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username).orElseThrow();

        task.setUser(user);
        Task savedTask = taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    /** UPDATE existing task */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username).orElseThrow();

        Optional<Task> taskOpt = taskRepository.findById(id);
        
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found"));
        }

        Task task = taskOpt.get();
        
        // Check if task belongs to current user
        if (!task.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized"));
        }

        // Update task fields
        task.setTitle(updatedTask.getTitle());
        task.setLearningTime(updatedTask.getLearningTime());
        task.setDate(updatedTask.getDate());
        
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    /** DELETE task */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username).orElseThrow();

        Optional<Task> taskOpt = taskRepository.findById(id);
        
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Task not found"));
        }

        Task task = taskOpt.get();
        
        // Check if task belongs to current user
        if (!task.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized"));
        }

        taskRepository.delete(task);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }

    // Helper method to get current username
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}