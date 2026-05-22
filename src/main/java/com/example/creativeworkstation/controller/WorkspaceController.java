package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.SystemConfig;
import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.SystemConfigRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {

    @Autowired
    private SystemConfigRepository configRepository;

    @Autowired
    private WorkProjectRepository projectRepository; // 需要用到上次写的 ProjectRepository

    // 获取工作台状态
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> response = new HashMap<>();

        // 获取工作模式
        String mode = configRepository.findById("workspace_mode")
                .map(SystemConfig::getConfigValue).orElse("SILENT_ORGANIZE");

        // 获取夜间模式
        boolean isDark = configRepository.findById("dark_mode")
                .map(c -> Boolean.parseBoolean(c.getConfigValue())).orElse(false);

        // 获取上次打开的项目 (如果有的话)
        WorkProject lastProject = null;
        String lastProjectId = configRepository.findById("last_project_id")
                .map(SystemConfig::getConfigValue).orElse(null);

        if (lastProjectId != null) {
            lastProject = projectRepository.findById(Long.parseLong(lastProjectId)).orElse(null);
        }

        response.put("currentMode", mode);
        response.put("darkMode", isDark);
        response.put("lastProject", lastProject);
        return response;
    }

    // 更新工作模式
    @PutMapping("/mode")
    public Map<String, Object> updateMode(@RequestBody Map<String, String> payload) {
        saveConfig("workspace_mode", payload.get("mode"));
        return Map.of("success", true);
    }

    // 更新夜间模式
    @PutMapping("/dark-mode")
    public Map<String, Object> updateDarkMode(@RequestBody Map<String, Boolean> payload) {
        saveConfig("dark_mode", String.valueOf(payload.get("darkMode")));
        return Map.of("success", true);
    }

    // 内部通用保存方法
    private void saveConfig(String key, String value) {
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        configRepository.save(config);
    }
}
