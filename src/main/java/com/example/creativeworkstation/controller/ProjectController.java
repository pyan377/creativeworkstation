package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.CreativeAsset;
import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.CreativeAssetRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
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

    @PostMapping
    public WorkProject create(@RequestBody WorkProject project) {
        if (project.getStatus() == null) {
            project.setStatus("草稿");
        }
        return projectRepository.save(project);
    }

    @GetMapping
    public List<WorkProject> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        if (category != null && status != null) {
            return projectRepository.findByCategoryAndStatus(category, status);
        } else if (category != null) {
            return projectRepository.findByCategory(category);
        } else if (status != null) {
            return projectRepository.findByStatus(status);
        }
        return projectRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkProject> getById(@PathVariable Long id) {
        Optional<WorkProject> project = projectRepository.findById(id);
        return project.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/assets")
    public List<CreativeAsset> getProjectAssets(@PathVariable Long id) {
        return assetRepository.findByProjectId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkProject> update(@PathVariable Long id, @RequestBody WorkProject projectData) {
        Optional<WorkProject> optionalProject = projectRepository.findById(id);
        if (optionalProject.isPresent()) {
            WorkProject project = optionalProject.get();
            project.setTitle(projectData.getTitle());
            project.setCategory(projectData.getCategory());
            project.setStatus(projectData.getStatus());
            project.setDescription(projectData.getDescription());
            project.setCoverImage(projectData.getCoverImage());
            project.setSourceFile(projectData.getSourceFile());
            project.setIsCandidate(projectData.getIsCandidate());
            return ResponseEntity.ok(projectRepository.save(project));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
