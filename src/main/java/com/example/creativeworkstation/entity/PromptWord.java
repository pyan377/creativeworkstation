package com.example.creativeworkstation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_prompt_word")
@Data
public class PromptWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 所属用户ID
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String tags;

    private Long projectId; // 关联的项目ID

    private Integer rating = 3; // 星级评分 1-5，默认3星

    private LocalDateTime createdAt = LocalDateTime.now();
}
