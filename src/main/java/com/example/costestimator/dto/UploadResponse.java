package com.example.costestimator.dto;

import com.example.costestimator.data.UploadStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record UploadResponse(
    Long id,
    String originalFilename,
    Long materialId,
    UploadStatus status,
    BigDecimal estimatedPrice,
    String errorMessage,
    Instant createdAt) {
}
