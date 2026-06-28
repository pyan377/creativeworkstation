package com.example.creativeworkstation.repository;

import com.example.creativeworkstation.entity.AssignmentTask;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentTaskRepository extends JpaRepository<AssignmentTask, Long> {

    @Query("SELECT t FROM AssignmentTask t WHERE t.userId = :userId " +
           "ORDER BY CASE WHEN t.deadline IS NULL THEN 1 ELSE 0 END, t.deadline ASC, t.priority ASC")
    List<AssignmentTask> findByUserIdOrderByDeadlineAscPriorityDesc(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM AssignmentTask t WHERE t.userId = :userId AND t.status <> 'DONE' " +
           "AND t.deadline IS NOT NULL AND t.deadline <= :threeDaysLater")
    long countUpcomingTasks(@Param("userId") Long userId, @Param("threeDaysLater") LocalDateTime threeDaysLater);

    @Query("SELECT t FROM AssignmentTask t WHERE t.userId = :userId AND t.status <> 'DONE' " +
           "AND t.deadline IS NOT NULL ORDER BY t.deadline ASC, t.priority ASC")
    List<AssignmentTask> findUrgentTasksByUserId(@Param("userId") Long userId, Pageable pageable);

    default Optional<AssignmentTask> findMostUrgentTask(Long userId) {
        List<AssignmentTask> tasks = findUrgentTasksByUserId(userId, Pageable.ofSize(1));
        return tasks.isEmpty() ? Optional.empty() : Optional.of(tasks.get(0));
    }

    List<AssignmentTask> findByProjectId(Long projectId);
}
