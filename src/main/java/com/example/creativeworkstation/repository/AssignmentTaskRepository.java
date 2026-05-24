package com.example.creativeworkstation.repository;

import com.example.creativeworkstation.entity.AssignmentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentTaskRepository extends JpaRepository<AssignmentTask, Long> {
    List<AssignmentTask> findByUserId(Long userId);

    // 查询即将截止的任务 (比如 3 天内)
    @Query("SELECT t FROM AssignmentTask t WHERE t.status = '待办' AND t.deadline <= :threeDaysLater AND t.deadline >= :now")
    List<AssignmentTask> findUpcomingTasks(LocalDateTime now, LocalDateTime threeDaysLater);

    // 查询所有未完成的任务
    List<AssignmentTask> findByStatus(String status);
}
