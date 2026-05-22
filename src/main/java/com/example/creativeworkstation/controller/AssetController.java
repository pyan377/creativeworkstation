package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.CreativeAsset;
import com.example.creativeworkstation.repository.CreativeAssetRepository;
import com.example.creativeworkstation.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "projectId", required = false) Long projectId) {
        try {
            CreativeAsset savedAsset = assetService.uploadAsset(file, projectId);
            return ResponseEntity.ok(savedAsset);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件保存失败: " + e.getMessage()));
        }
    }

    @GetMapping
    public List<CreativeAsset> getAll() {
        return assetRepository.findAll();
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        try {
            System.out.println("请求预览资产 ID: " + id);
            
            CreativeAsset asset = assetRepository.findById(id).orElse(null);
            if (asset == null) {
                System.out.println("未找到 ID 为 " + id + " 的资产记录");
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("找到资产: " + asset.getFileName());
            System.out.println("保存的文件路径: " + asset.getFilePath());

            File file = new File(asset.getFilePath());
            System.out.println("解析后的文件对象: " + file.getAbsolutePath());
            System.out.println("文件是否存在: " + file.exists());
            System.out.println("文件是否可读: " + file.canRead());
            
            if (!file.exists()) {
                System.out.println("文件不存在于磁盘上");
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
            System.out.println("预览文件时发生错误:");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAssets(@RequestParam String ids) {
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
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (CreativeAsset asset : assets) {
                    File file = new File(asset.getFilePath());
                    if (file.exists()) {
                        ZipEntry zipEntry = new ZipEntry(asset.getFileName());
                        zos.putNextEntry(zipEntry);
                        Files.copy(file.toPath(), zos);
                        zos.closeEntry();
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
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (assetRepository.existsById(id)) {
            assetRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
