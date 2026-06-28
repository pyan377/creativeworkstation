package com.example.creativeworkstation.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchAssignRequest {
    private List<Long> assetIds;
    private Long projectId;
}
