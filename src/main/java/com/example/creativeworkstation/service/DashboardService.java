package com.example.creativeworkstation.service;

import com.example.creativeworkstation.entity.AssignmentTask;
import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.AssignmentTaskRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class DashboardService {

    @Autowired
    private WorkProjectRepository projectRepository;

    @Autowired
    private AssignmentTaskRepository taskRepository;

    public Map<String, Object> getSummaryStats(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime threeDaysLater = now.plusDays(3);

        long recentProjectsCount = projectRepository.countByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);
        long upcomingTasksCount = taskRepository.countUpcomingTasks(userId, threeDaysLater);
        long candidateProjectsCount = projectRepository.countByUserIdAndIsCandidateTrue(userId);
        long missingSourceFilesCount = projectRepository.countByUserIdAndSourceFileIsNull(userId);
        long missingDescriptionCount = projectRepository.countByUserIdAndDescriptionIsNull(userId);

        Optional<AssignmentTask> urgentTask = taskRepository.findMostUrgentTask(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("recentCount", recentProjectsCount);
        result.put("upcomingTasks", upcomingTasksCount);
        result.put("taskCount", upcomingTasksCount);
        result.put("candidateCount", candidateProjectsCount);
        result.put("missingFiles", missingSourceFilesCount);
        result.put("missingDesc", missingDescriptionCount);
        result.put("urgentTask", urgentTask.orElse(null));

        Map<String, Long> overview = Map.of(
                "recentProjectsCount", recentProjectsCount,
                "upcomingTasksCount", upcomingTasksCount,
                "candidateProjectsCount", candidateProjectsCount
        );
        Map<String, Long> targets = Map.of(
                "missingSourceFilesCount", missingSourceFilesCount,
                "missingDescriptionCount", missingDescriptionCount,
                "upcomingTasksCount", upcomingTasksCount,
                "completedReadyToJoinCount", 0L
        );
        result.put("overview", overview);
        result.put("targets", targets);

        return result;
    }

    public Optional<WorkProject> getRandomIncompleteProject(Long userId) {
        List<WorkProject> projectsMissingSource = projectRepository.findByUserIdAndSourceFileIsNull(userId);
        List<WorkProject> projectsMissingDescription = projectRepository.findByUserIdAndDescriptionIsNull(userId);

        List<WorkProject> allIncompleteProjects = new java.util.ArrayList<>(projectsMissingSource);
        for (WorkProject p : projectsMissingDescription) {
            if (!allIncompleteProjects.contains(p)) {
                allIncompleteProjects.add(p);
            }
        }

        List<WorkProject> filteredProjects = allIncompleteProjects.stream()
                .filter(project -> {
                    String status = project.getStatus();
                    return "草稿".equals(status) || "进行中".equals(status);
                })
                .toList();

        if (filteredProjects.isEmpty()) {
            return Optional.empty();
        }

        Random random = new Random();
        int randomIndex = random.nextInt(filteredProjects.size());
        return Optional.of(filteredProjects.get(randomIndex));
    }
}
