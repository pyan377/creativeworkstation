package com.example.creativeworkstation.service;

import com.example.creativeworkstation.config.R2Properties;
import com.example.creativeworkstation.entity.CreativeAsset;
import com.example.creativeworkstation.entity.WorkProject;
import com.example.creativeworkstation.repository.CreativeAssetRepository;
import com.example.creativeworkstation.repository.WorkProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AssetService {

    @Autowired
    private R2Properties r2Properties;

    @Autowired
    private S3Client s3Client;

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

    public boolean isR2Asset(CreativeAsset asset) {
        String fileUrl = asset.getFileUrl();
        return fileUrl != null && (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"));
    }

    public InputStream openAssetStream(CreativeAsset asset) throws IOException {
        if (isR2Asset(asset)) {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(r2Properties.getBucketName())
                    .key(asset.getFilePath())
                    .build());
        }
        Path localPath = Paths.get(asset.getFilePath());
        if (!Files.exists(localPath)) {
            throw new IOException("文件不存在");
        }
        return Files.newInputStream(localPath);
    }

    private void deletePhysicalFile(CreativeAsset asset) {
        if (asset.getFilePath() == null || asset.getFilePath().isBlank()) {
            return;
        }
        if (isR2Asset(asset)) {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(r2Properties.getBucketName())
                        .key(asset.getFilePath())
                        .build());
            } catch (Exception ignored) {
                // 物理文件删除失败不阻断数据库记录清理
            }
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(asset.getFilePath()));
        } catch (IOException ignored) {
            // 物理文件删除失败不阻断数据库记录清理
        }
    }

    private CreativeAsset saveFile(MultipartFile file, Long projectId, String assetCategory, Long userId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }

        String storedFileName = UUID.randomUUID().toString() + (fileExtension.isEmpty() ? "" : "." + fileExtension);
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(r2Properties.getBucketName())
                        .key(storedFileName)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        CreativeAsset asset = new CreativeAsset();
        asset.setUserId(userId);
        asset.setProjectId(projectId);
        asset.setFileName(originalFilename);
        asset.setFileSize(file.getSize());
        asset.setFileType(fileExtension);
        asset.setAssetCategory(assetCategory);
        asset.setFilePath(storedFileName);
        asset.setFileUrl(buildPublicUrl(storedFileName));

        return assetRepository.save(asset);
    }

    private String buildPublicUrl(String objectKey) {
        String domain = r2Properties.getPublicDomain();
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        return domain + "/" + objectKey;
    }
}
