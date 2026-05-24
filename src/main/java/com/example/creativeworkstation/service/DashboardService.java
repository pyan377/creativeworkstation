package com.example.creativeworkstation.service;

import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.AssignmentTaskRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
import com.example.creativeworkstation.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        // 1. 概览统计
        long recentProjectsCount = projectRepository.countByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);
        long upcomingTasksCount = 0; // 暂时0，等添加用户后再更新
        long candidateProjectsCount = projectRepository.countByUserIdAndIsCandidateTrue(userId);

        // 2. 今日整理目标
        long missingSourceFilesCount = projectRepository.countByUserIdAndSourceFileIsNull(userId);
        long missingDescriptionCount = projectRepository.countByUserIdAndDescriptionIsNull(userId);

        // 3. 待完善作品总数
        long incompleteProjectsCount = missingSourceFilesCount + missingDescriptionCount;

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

        return Map.of("overview", overview, "targets", targets);
    }

    public Optional<WorkProject> getRandomIncompleteProject(Long userId) {
        // 1. 查找所有"待完善"的作品
        List<WorkProject> projectsMissingSource = projectRepository.findByUserIdAndSourceFileIsNull(userId);
        List<WorkProject> projectsMissingDescription = projectRepository.findByUserIdAndDescriptionIsNull(userId);

        // 合并列表，并去重
        List<WorkProject> allIncompleteProjects = new java.util.ArrayList<>(projectsMissingSource);
        for(WorkProject p : projectsMissingDescription) {
            if (!allIncompleteProjects.contains(p)) {
                allIncompleteProjects.add(p);
            }
        }

        // 2. 过滤状态：只保留"草稿"和"进行中"的项目
        List<WorkProject> filteredProjects = allIncompleteProjects.stream()
                .filter(project -> {
                    String status = project.getStatus();
                    return "草稿".equals(status) || "进行中".equals(status);
                })
                .toList();

        if (filteredProjects.isEmpty()) {
            return Optional.empty();
        }

        // 3. 随机抽取一个
        Random random = new Random();
        int randomIndex = random.nextInt(filteredProjects.size());
        return Optional.of(filteredProjects.get(randomIndex));
    }
}
