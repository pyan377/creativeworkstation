package com.example.creativeworkstation.service;

import com.example.creativeworkstation.entity.CreativeAsset;
import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.CreativeAssetRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkProjectService {

    @Autowired
    private WorkProjectRepository projectRepository;

    @Autowired
    private CreativeAssetRepository assetRepository;

    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        WorkProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));

        if (!userId.equals(project.getUserId())) {
            throw new IllegalArgumentException("无权限删除该项目");
        }

        List<CreativeAsset> linkedAssets = assetRepository.findByUserIdAndProjectId(userId, projectId);
        for (CreativeAsset asset : linkedAssets) {
            asset.setProjectId(null);
            assetRepository.save(asset);
        }

        projectRepository.delete(project);
    }
}
