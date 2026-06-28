package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.AssignmentTask;
import com.example.creativeworkstation.repository.AssignmentTaskRepository;
import com.example.creativeworkstation.service.AssignmentTaskService;
import com.example.creativeworkstation.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class AssignmentTaskController {

    @Autowired
    private AssignmentTaskRepository taskRepository;

    @Autowired
    private AssignmentTaskService taskService;

    @PostMapping
    public ResponseEntity<AssignmentTask> create(@RequestBody AssignmentTask task, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        task.setUserId(userId);
        if (task.getStatus() == null) {
            task.setStatus("TODO");
        }
        if (task.getPriority() == null) {
            task.setPriority(2);
        }
        AssignmentTask saved = taskRepository.save(task);
        taskService.enrichTask(saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<AssignmentTask>> getAll(HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(taskService.getTasksByUserId(userId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AssignmentTask>> getByProjectId(@PathVariable Long projectId, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(taskService.getTasksByProjectId(projectId, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentTask> getById(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<AssignmentTask> task = taskRepository.findById(id);
        if (task.isPresent() && userId.equals(task.get().getUserId())) {
            taskService.enrichTask(task.get());
            return ResponseEntity.ok(task.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentTask> update(@PathVariable Long id, @RequestBody AssignmentTask taskData, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<AssignmentTask> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty() || !userId.equals(optionalTask.get().getUserId())) {
            return ResponseEntity.notFound().build();
        }
        AssignmentTask task = optionalTask.get();
        if (taskData.getTitle() != null) {
            task.setTitle(taskData.getTitle());
        }
        if (taskData.getDescription() != null) {
            task.setDescription(taskData.getDescription());
        }
        if (taskData.getPlatform() != null) {
            task.setPlatform(taskData.getPlatform());
        }
        if (taskData.getTaskType() != null) {
            task.setTaskType(taskData.getTaskType());
        }
        if (taskData.getStatus() != null) {
            task.setStatus(taskData.getStatus());
        }
        if (taskData.getPriority() != null) {
            task.setPriority(taskData.getPriority());
        }
        if (taskData.getDeadline() != null) {
            task.setDeadline(taskData.getDeadline());
        }
        if (taskData.getExpectedRevenue() != null) {
            task.setExpectedRevenue(taskData.getExpectedRevenue());
        }
        if (taskData.getProjectId() != null) {
            task.setProjectId(taskData.getProjectId());
        }
        AssignmentTask saved = taskRepository.save(task);
        taskService.enrichTask(saved);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<AssignmentTask> task = taskRepository.findById(id);
        if (task.isPresent() && userId.equals(task.get().getUserId())) {
            taskRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
