package com.example.creativeworkstation.repository;

import com.example.creativeworkstation.entity.CreativeAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreativeAssetRepository extends JpaRepository<CreativeAsset, Long> {
    // 根据项目ID查询关联素材
    List<CreativeAsset> findByProjectId(Long projectId);
}
