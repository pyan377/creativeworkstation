package com.example.creativeworkstation.service;

import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.AssignmentTaskRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
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

    public Map<String, Object> getSummaryStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime threeDaysLater = now.plusDays(3);

        // 1. 概览统计
        long recentProjectsCount = projectRepository.countByCreatedAtAfter(sevenDaysAgo);
        long upcomingTasksCount = taskRepository.findUpcomingTasks(now, threeDaysLater).size();
        long candidateProjectsCount = projectRepository.countByIsCandidateTrue(); // 假设已实现

        // 2. 今日整理目标
        long missingSourceFilesCount = projectRepository.countBySourceFileIsNull();
        long missingDescriptionCount = projectRepository.countByDescriptionIsNull(); // 假设已实现
        // 任务即将截止，复用 upcomingTasksCount

        // 3. 待完善作品总数 (缺少源文件或描述)
        // 注意：这里需要 JPA Query 来实现 (count by sourceFile is null OR description is null)
        // 为简化，暂且用上面两个累加，实际应更精确
        long incompleteProjectsCount = missingSourceFilesCount + missingDescriptionCount;

        Map<String, Long> overview = Map.of(
                "recentProjectsCount", recentProjectsCount,
                "upcomingTasksCount", upcomingTasksCount,
                "candidateProjectsCount", candidateProjectsCount
        );

        Map<String, Long> targets = Map.of(
                "missingSourceFilesCount", missingSourceFilesCount,
                "missingDescriptionCount", missingDescriptionCount,
                "upcomingTasksCount", upcomingTasksCount, // 同样作为目标显示
                "completedReadyToJoinCount", 0L // 这个字段在需求中未明确，先设为0
        );

        return Map.of("overview", overview, "targets", targets);
    }

    public Optional<WorkProject> getRandomIncompleteProject() {
        // 1. 查找所有"待完善"的作品
        // 实际生产中，这里需要一个更优的查询，一次性获取 (sourceFile IS NULL OR description IS NULL) 的项目
        // 为演示，我们先通过两次查询，然后合并，再随机选一个
        List<WorkProject> projectsMissingSource = projectRepository.findBySourceFileIsNull();
        List<WorkProject> projectsMissingDescription = projectRepository.findByDescriptionIsNull();

        // 合并列表，并去重 (如果一个项目同时缺少源文件和描述)
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