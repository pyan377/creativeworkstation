package com.example.creativeworkstation.service;

import com.example.creativeworkstation.entity.CreativeAsset;
import com.example.creativeworkstation.repository.CreativeAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class AssetService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private CreativeAssetRepository assetRepository;

    public CreativeAsset uploadAsset(MultipartFile file, Long projectId, Long userId) throws IOException {
        // 1. 确保物理文件夹存在
        Path storageDirectory = Paths.get(uploadDir);
        if (!Files.exists(storageDirectory)) {
            Files.createDirectories(storageDirectory);
        }

        // 2. 解析文件信息并生成安全的文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }

        // 为了防止文件名冲突，使用 UUID 重新命名文件存储
        String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
        Path targetLocation = storageDirectory.resolve(newFileName);

        // 3. 将文件写入本地磁盘
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 4. 将记录存入数据库
        CreativeAsset asset = new CreativeAsset();
        asset.setUserId(userId);
        asset.setProjectId(projectId);
        asset.setFileName(originalFilename);
        asset.setFileSize(file.getSize());
        asset.setFileType(fileExtension);
        // 保存完整的物理路径
        asset.setFilePath(targetLocation.toAbsolutePath().toString());

        return assetRepository.save(asset);
    }
}
