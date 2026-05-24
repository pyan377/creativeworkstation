package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.service.DashboardService;
import com.example.creativeworkstation.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> getSummary(HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(dashboardService.getSummaryStats(userId));
    }

    // 随机抽取一个待完善任务 ("抽一个"功能)
    @GetMapping("/random-target")
    public ResponseEntity<Optional<WorkProject>> getRandomTarget(HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(dashboardService.getRandomIncompleteProject(userId));
    }
}
