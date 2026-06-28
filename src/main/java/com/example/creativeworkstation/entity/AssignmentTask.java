package com.example.creativeworkstation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
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

    @Column(columnDefinition = "TEXT")
    private String description;

    private String platform;

    /** 自定义任务类型，如：商单、日常、活动、外包等 */
    private String taskType;

    /** TODO / DOING / REVIEW / DONE */
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'TODO'")
    private String status = "TODO";

    /** 1=高, 2=中, 3=低 */
    private Integer priority = 2;

    private LocalDateTime deadline;

    @Column(precision = 12, scale = 2)
    private BigDecimal expectedRevenue;

    private Long projectId;

    @Transient
    private String projectName;

    private Long userId;

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
        if (status == null) {
            status = "TODO";
        }
        if (priority == null) {
            priority = 2;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
