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

    private Long projectId;
    private String fileName;

    // 存储前端可以直接访问的URL，如 /files/xxx.png
    private String filePath;

    private Long fileSize;
    private String fileType;
    private LocalDateTime createdAt = LocalDateTime.now();
}