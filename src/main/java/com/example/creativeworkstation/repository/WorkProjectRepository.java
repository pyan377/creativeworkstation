package com.example.creativeworkstation.repository;

import com.example.creativeworkstation.entity.WorkProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkProjectRepository extends JpaRepository<WorkProject, Long> {

    // 1. 统计近7天创建的作品数
    long countByCreatedAtAfter(LocalDateTime date);

    // 2. 统计候选作品总数
    long countByIsCandidateTrue();

    // 3. 统计缺少源文件的项目数量 (用于 Dashboard 统计)
    long countBySourceFileIsNull();

    // 4. 统计缺少描述的项目数量 (用于 Dashboard 统计)
    long countByDescriptionIsNull();

    // 5. 获取所有缺少源文件的项目列表 (用于“抽一个”逻辑)
    List<WorkProject> findBySourceFileIsNull();

    // 6. 获取所有缺少描述的项目列表 (用于“抽一个”逻辑)
    List<WorkProject> findByDescriptionIsNull();

    // 7. (可选) 按标题模糊搜索
    List<WorkProject> findByTitleContaining(String keyword);

    // 8. 按分类查询
    List<WorkProject> findByCategory(String category);

    // 9. 按状态查询
    List<WorkProject> findByStatus(String status);

    // 10. 按分类和状态查询
    List<WorkProject> findByCategoryAndStatus(String category, String status);
    
    // 11. 查询状态在指定列表中的项目
    List<WorkProject> findByStatusIn(List<String> statuses);
}
