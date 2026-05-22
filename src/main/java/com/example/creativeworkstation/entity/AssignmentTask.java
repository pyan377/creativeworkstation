package com.example.creativeworkstation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_assignment_task")
@Data
public class AssignmentTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private LocalDateTime deadline;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT '待办'")
    private String status = "待办"; // 默认值

    // 外键关联tb_work_project，允许为空
    private Long projectId;

    private LocalDateTime createdAt = LocalDateTime.now();
}