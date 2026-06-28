package com.example.creativeworkstation.service;

import com.example.creativeworkstation.entity.AssignmentTask;
import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.AssignmentTaskRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssignmentTaskService {

    @Autowired
    private AssignmentTaskRepository taskRepository;

    @Autowired
    private WorkProjectRepository projectRepository;

    public List<AssignmentTask> getTasksByUserId(Long userId) {
        List<AssignmentTask> tasks = taskRepository.findByUserIdOrderByDeadlineAscPriorityDesc(userId);
        enrichWithProjectName(tasks);
        return tasks;
    }

    public List<AssignmentTask> getTasksByProjectId(Long projectId, Long userId) {
        Optional<WorkProject> project = projectRepository.findById(projectId);
        if (project.isEmpty() || !userId.equals(project.get().getUserId())) {
            return List.of();
        }
        List<AssignmentTask> tasks = taskRepository.findByProjectId(projectId).stream()
                .filter(task -> userId.equals(task.getUserId()))
                .toList();
        enrichWithProjectName(tasks);
        return tasks;
    }

    public Optional<AssignmentTask> enrichTask(AssignmentTask task) {
        if (task.getProjectId() != null) {
            projectRepository.findById(task.getProjectId())
                    .ifPresent(project -> task.setProjectName(project.getTitle()));
        }
        return Optional.of(task);
    }

    private void enrichWithProjectName(List<AssignmentTask> tasks) {
        for (AssignmentTask task : tasks) {
            if (task.getProjectId() != null) {
                projectRepository.findById(task.getProjectId())
                        .ifPresent(project -> task.setProjectName(project.getTitle()));
            }
        }
    }
}
