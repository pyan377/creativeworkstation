package com.example.creativeworkstation.repository;

import com.example.creativeworkstation.entity.PromptWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptWordRepository extends JpaRepository<PromptWord, Long> {
    List<PromptWord> findByProjectIdOrderByRatingDescCreatedAtDesc(Long projectId);
}