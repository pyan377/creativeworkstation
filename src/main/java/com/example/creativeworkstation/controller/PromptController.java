package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.PromptWord;
import com.example.creativeworkstation.repository.PromptWordRepository;
import com.example.creativeworkstation.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    @Autowired
    private PromptWordRepository promptRepository;

    // 快速保存提示词
    @PostMapping
    public ResponseEntity<PromptWord> createPrompt(@RequestBody PromptWord promptWord, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        promptWord.setUserId(userId);
        if (promptWord.getRating() == null) {
            promptWord.setRating(3);
        }
        return ResponseEntity.ok(promptRepository.save(promptWord));
    }

    // 扩展：获取提示词列表 (按时间倒序)
    @GetMapping
    public ResponseEntity<List<PromptWord>> getAllPrompts(HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(promptRepository.findByUserId(userId));
    }

    // 获取指定项目的提示词（按星级降序，然后时间降序）
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<PromptWord>> getPromptsByProjectId(@PathVariable Long projectId, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(promptRepository.findByUserIdAndProjectIdOrderByRatingDescCreatedAtDesc(userId, projectId));
    }

    // 删除提示词
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Optional<PromptWord> prompt = promptRepository.findById(id);
        if (prompt.isPresent() && userId.equals(prompt.get().getUserId())) {
            promptRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 更新提示词星级
    @PutMapping("/{id}/rating")
    public ResponseEntity<PromptWord> updateRating(@PathVariable Long id, @RequestBody Map<String, Integer> request, HttpSession session) {
        Long userId = SessionUtil.getCurrentUserId(session);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        PromptWord prompt = promptRepository.findById(id).orElse(null);
        if (prompt != null && userId.equals(prompt.getUserId())) {
            Integer rating = request.get("rating");
            if (rating != null && rating >= 1 && rating <= 5) {
                prompt.setRating(rating);
                return ResponseEntity.ok(promptRepository.save(prompt));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
