package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // 获取首页概览与统计数据
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return dashboardService.getSummaryStats();
    }

    // 随机抽取一个待完善任务 ("抽一个"功能)
    @GetMapping("/random-target")
    public Optional<WorkProject> getRandomTarget() {
        return dashboardService.getRandomIncompleteProject();
    }
}
