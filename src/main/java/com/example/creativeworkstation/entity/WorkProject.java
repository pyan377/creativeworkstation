package com.example.creativeworkstation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_work_project")
@Data
public class WorkProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String category;
    private String status;
    private String coverImage;
    private String sourceFile;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String tags;
    private Boolean isCandidate = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}
