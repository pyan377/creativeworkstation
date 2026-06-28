package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.dto.BatchAssignRequest;
import com.example.creativeworkstation.dto.BatchDeleteRequest;
import com.example.creativeworkstation.entity.CreativeAsset;
import com.example.creativeworkstation.repository.CreativeAssetRepository;
import com.example.creativeworkstation.service.AssetService;
import com.example.creativeworkstation.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private CreativeAssetRepository assetRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "assetCategory", required = false) String assetCategory,
            HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            if (files != null && files.length > 0) {
                List<CreativeAsset> savedAssets = assetService.uploadAssets(files, assetCategory, userId);
                return ResponseEntity.ok(savedAssets);
            }
            if (file != null && !file.isEmpty()) {
                CreativeAsset savedAsset = assetService.uploadAsset(file, projectId, assetCategory, userId);
                return ResponseEntity.ok(savedAsset);
            }
            return ResponseEntity.badRequest().body(Map.of("error", "请选择要上传的文件"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件保存失败: " + e.getMessage()));
        }
    }

    @PutMapping("/batch-assign")
    public ResponseEntity<?> batchAssign(@RequestBody BatchAssignRequest request, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (request.getProjectId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请选择目标作品"));
        }

        try {
            List<CreativeAsset> updated = assetService.batchAssign(request.getAssetIds(), request.getProjectId(), userId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "批量关联失败: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<CreativeAsset>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Boolean isAssigned,
            @RequestParam(required = false) Boolean unassigned,
            HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(assetService.getAssets(userId, category, projectId, isAssigned, unassigned));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            CreativeAsset asset = assetRepository.findById(id).orElse(null);
            if (asset == null || !userId.equals(asset.getUserId())) {
                return ResponseEntity.notFound().build();
            }

            if (assetService.isR2Asset(asset)) {
                return ResponseEntity.status(302)
                        .location(URI.create(asset.getFileUrl()))
                        .build();
            }

            java.io.File file = new java.io.File(asset.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + asset.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAssets(@RequestParam String ids, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            List<Long> assetIds = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();

            if (assetIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            List<CreativeAsset> assets = assetRepository.findAllById(assetIds);
            for (CreativeAsset asset : assets) {
                if (!userId.equals(asset.getUserId())) {
                    return ResponseEntity.status(403).build();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (CreativeAsset asset : assets) {
                    try (InputStream inputStream = assetService.openAssetStream(asset)) {
                        ZipEntry zipEntry = new ZipEntry(asset.getFileName());
                        zos.putNextEntry(zipEntry);
                        inputStream.transferTo(zos);
                        zos.closeEntry();
                    } catch (Exception ignored) {
                        // 单个文件读取失败时跳过，继续打包其余文件
                    }
                }
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "assets_" + timestamp + ".zip";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/batch")
    public ResponseEntity<?> batchDelete(@RequestBody BatchDeleteRequest request, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            assetService.batchDelete(request.getIds(), userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "批量删除失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            assetService.deleteAsset(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
