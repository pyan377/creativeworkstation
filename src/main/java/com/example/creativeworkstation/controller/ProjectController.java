package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.CreativeAsset;
import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.CreativeAssetRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
import com.example.creativeworkstation.service.WorkProjectService;
import com.example.creativeworkstation.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    @Autowired
    private WorkProjectRepository projectRepository;

    @Autowired
    private CreativeAssetRepository assetRepository;

    @Autowired
    private WorkProjectService projectService;

    @PostMapping
    public ResponseEntity<WorkProject> create(@RequestBody WorkProject project, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        project.setUserId(userId);
        if (project.getStatus() == null) {
            project.setStatus("草稿");
        }
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @GetMapping
    public ResponseEntity<List<WorkProject>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        if (category != null && status != null) {
            return ResponseEntity.ok(projectRepository.findByUserIdAndCategoryAndStatus(userId, category, status));
        } else if (category != null) {
            return ResponseEntity.ok(projectRepository.findByUserIdAndCategory(userId, category));
        } else if (status != null) {
            return ResponseEntity.ok(projectRepository.findByUserIdAndStatus(userId, status));
        }
        return ResponseEntity.ok(projectRepository.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkProject> getById(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<WorkProject> project = projectRepository.findById(id);
        if (project.isPresent() && userId.equals(project.get().getUserId())) {
            return ResponseEntity.ok(project.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/assets")
    public ResponseEntity<List<CreativeAsset>> getProjectAssets(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        // 先检查项目是否属于当前用户
        Optional<WorkProject> project = projectRepository.findById(id);
        if (!project.isPresent() || !userId.equals(project.get().getUserId())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(assetRepository.findByUserIdAndProjectId(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkProject> update(@PathVariable Long id, @RequestBody WorkProject projectData, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<WorkProject> optionalProject = projectRepository.findById(id);
        if (optionalProject.isPresent() && userId.equals(optionalProject.get().getUserId())) {
            WorkProject project = optionalProject.get();
            project.setTitle(projectData.getTitle());
            project.setCategory(projectData.getCategory());
            project.setStatus(projectData.getStatus());
            project.setDescription(projectData.getDescription());
            project.setCoverImage(projectData.getCoverImage());
            project.setSourceFile(projectData.getSourceFile());
            project.setIsCandidate(projectData.getIsCandidate());
            project.setTags(projectData.getTags());
            return ResponseEntity.ok(projectRepository.save(project));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<WorkProject> project = projectRepository.findById(id);
        if (project.isPresent() && userId.equals(project.get().getUserId())) {
            try {
                projectService.deleteProject(id, userId);
                return ResponseEntity.ok().build();
            } catch (IllegalArgumentException e) {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.notFound().build();
    }
}
