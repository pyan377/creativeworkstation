package com.example.creativeworkstation.controller;

import com.example.creativeworkstation.entity.PromptWord;
import com.example.creativeworkstation.repository.PromptWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    @Autowired
    private PromptWordRepository promptRepository;

    // 快速保存提示词
    @PostMapping
    public PromptWord createPrompt(@RequestBody PromptWord promptWord) {
        if (promptWord.getRating() == null) {
            promptWord.setRating(3);
        }
        return promptRepository.save(promptWord);
    }

    // 扩展：获取提示词列表 (按时间倒序)
    @GetMapping
    public List<PromptWord> getAllPrompts() {
        return promptRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    // 获取指定项目的提示词（按星级降序，然后时间降序）
    @GetMapping("/project/{projectId}")
    public List<PromptWord> getPromptsByProjectId(@PathVariable Long projectId) {
        return promptRepository.findByProjectIdOrderByRatingDescCreatedAtDesc(projectId);
    }

    // 删除提示词
    @DeleteMapping("/{id}")
    public void deletePrompt(@PathVariable Long id) {
        promptRepository.deleteById(id);
    }

    // 更新提示词星级
    @PutMapping("/{id}/rating")
    public PromptWord updateRating(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        PromptWord prompt = promptRepository.findById(id).orElse(null);
        if (prompt != null) {
            Integer rating = request.get("rating");
            if (rating != null && rating >= 1 && rating <= 5) {
                prompt.setRating(rating);
                return promptRepository.save(prompt);
            }
        }
        return prompt;
    }
}
