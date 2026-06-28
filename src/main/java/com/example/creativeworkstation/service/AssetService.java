package com.example.creativeworkstation.service;

import com.example.creativeworkstation.entity.CreativeAsset;
import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.CreativeAssetRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AssetService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private CreativeAssetRepository assetRepository;

    @Autowired
    private WorkProjectRepository projectRepository;

    public CreativeAsset uploadAsset(MultipartFile file, Long projectId, Long userId) throws IOException {
        return uploadAsset(file, projectId, null, userId);
    }

    public CreativeAsset uploadAsset(MultipartFile file, Long projectId, String assetCategory, Long userId) throws IOException {
        return saveFile(file, projectId, assetCategory, userId);
    }

    public List<CreativeAsset> uploadAssets(MultipartFile[] files, String assetCategory, Long userId) throws IOException {
        List<CreativeAsset> saved = new ArrayList<>();
        if (files == null) {
            return saved;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saved.add(saveFile(file, null, assetCategory, userId));
            }
        }
        return saved;
    }

    public List<CreativeAsset> batchAssign(List<Long> assetIds, Long projectId, Long userId) {
        Optional<WorkProject> project = projectRepository.findById(projectId);
        if (project.isEmpty() || !userId.equals(project.get().getUserId())) {
            throw new IllegalArgumentException("目标作品不存在或无权限");
        }
        if (assetIds == null || assetIds.isEmpty()) {
            throw new IllegalArgumentException("请选择至少一个素材");
        }

        List<CreativeAsset> assets = assetRepository.findAllById(assetIds);
        List<CreativeAsset> updated = new ArrayList<>();
        for (CreativeAsset asset : assets) {
            if (!userId.equals(asset.getUserId())) {
                throw new IllegalArgumentException("包含无权限操作的素材");
            }
            asset.setProjectId(projectId);
            updated.add(assetRepository.save(asset));
        }
        return updated;
    }

    public void batchDelete(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("请选择至少一个素材");
        }

        List<CreativeAsset> assets = assetRepository.findAllById(ids);
        for (CreativeAsset asset : assets) {
            if (!userId.equals(asset.getUserId())) {
                throw new IllegalArgumentException("包含无权限操作的素材");
            }
            deletePhysicalFile(asset);
            assetRepository.delete(asset);
        }
    }

    public void deleteAsset(Long id, Long userId) {
        CreativeAsset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("素材不存在"));
        if (!userId.equals(asset.getUserId())) {
            throw new IllegalArgumentException("无权限删除该素材");
        }
        deletePhysicalFile(asset);
        assetRepository.delete(asset);
    }

    public List<CreativeAsset> getAssets(Long userId, String category, Long projectId, Boolean isAssigned) {
        boolean hasCategory = category != null && !category.isBlank();

        if (projectId != null) {
            if (hasCategory) {
                return assetRepository.findByUserIdAndAssetCategoryAndProjectId(userId, category, projectId);
            }
            return assetRepository.findByUserIdAndProjectId(userId, projectId);
        }

        if (Boolean.TRUE.equals(isAssigned)) {
            if (hasCategory) {
                return assetRepository.findByUserIdAndAssetCategoryAndProjectIdIsNotNull(userId, category);
            }
            return assetRepository.findByUserIdAndProjectIdIsNotNull(userId);
        }

        if (Boolean.FALSE.equals(isAssigned)) {
            if (hasCategory) {
                return assetRepository.findByUserIdAndAssetCategoryAndProjectIdIsNull(userId, category);
            }
            return assetRepository.findByUserIdAndProjectIdIsNull(userId);
        }

        if (hasCategory) {
            return assetRepository.findByUserIdAndAssetCategory(userId, category);
        }
        return assetRepository.findByUserId(userId);
    }

    /** 兼容旧参数 unassigned=true */
    public List<CreativeAsset> getAssets(Long userId, String category, Long projectId, Boolean isAssigned, Boolean unassigned) {
        if (Boolean.TRUE.equals(unassigned)) {
            return getAssets(userId, category, projectId, false);
        }
        return getAssets(userId, category, projectId, isAssigned);
    }

    private void deletePhysicalFile(CreativeAsset asset) {
        if (asset.getFilePath() == null || asset.getFilePath().isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(asset.getFilePath()));
        } catch (IOException ignored) {
            // 物理文件删除失败不阻断数据库记录清理
        }
    }

    private CreativeAsset saveFile(MultipartFile file, Long projectId, String assetCategory, Long userId) throws IOException {
        Path storageDirectory = Paths.get(uploadDir);
        if (!Files.exists(storageDirectory)) {
            Files.createDirectories(storageDirectory);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }

        String storedFileName = UUID.randomUUID().toString() + (fileExtension.isEmpty() ? "" : "." + fileExtension);
        Path targetLocation = storageDirectory.resolve(storedFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        CreativeAsset asset = new CreativeAsset();
        asset.setUserId(userId);
        asset.setProjectId(projectId);
        asset.setFileName(originalFilename);
        asset.setFileSize(file.getSize());
        asset.setFileType(fileExtension);
        asset.setAssetCategory(assetCategory);
        asset.setFilePath(targetLocation.toAbsolutePath().toString());
        asset.setFileUrl("/uploads/" + storedFileName);

        return assetRepository.save(asset);
    }
}
