package com.example.creativeworkstation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_creative_asset")
@Data
public class CreativeAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    /** 关联作品 ID，为空表示未分配至作品 */
    private Long projectId;

    private String fileName;

    /** 前端可直接访问的 URL，如 /uploads/xxx.png */
    private String fileUrl;

    /** 本地磁盘绝对路径，供预览/导出使用 */
    private String filePath;

    /** 分类：DESIGN / VIDEO / PHOTO */
    private String assetCategory;

    private Long fileSize;
    private String fileType;

    private LocalDateTime uploadTime;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (uploadTime == null) {
            uploadTime = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }
}
