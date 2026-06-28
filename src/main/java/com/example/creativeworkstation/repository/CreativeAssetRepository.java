package com.example.creativeworkstation.repository;

import com.example.creativeworkstation.entity.CreativeAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreativeAssetRepository extends JpaRepository<CreativeAsset, Long> {

    List<CreativeAsset> findByProjectId(Long projectId);

    List<CreativeAsset> findByAssetCategory(String assetCategory);

    List<CreativeAsset> findByProjectIdIsNull();

    List<CreativeAsset> findByUserId(Long userId);

    List<CreativeAsset> findByUserIdAndProjectId(Long userId, Long projectId);

    List<CreativeAsset> findByUserIdAndAssetCategory(Long userId, String assetCategory);

    List<CreativeAsset> findByUserIdAndProjectIdIsNull(Long userId);

    List<CreativeAsset> findByUserIdAndAssetCategoryAndProjectIdIsNull(Long userId, String assetCategory);

    List<CreativeAsset> findByUserIdAndAssetCategoryAndProjectId(Long userId, String assetCategory, Long projectId);

    List<CreativeAsset> findByUserIdAndProjectIdIsNotNull(Long userId);

    List<CreativeAsset> findByUserIdAndAssetCategoryAndProjectIdIsNotNull(Long userId, String assetCategory);

    long countByUserId(Long userId);
}
