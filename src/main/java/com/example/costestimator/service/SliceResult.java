package com.example.costestimator.service;

import java.math.BigDecimal;

public record SliceResult(
    long seconds,
    BigDecimal filamentCm3,
    String slicerVersion) {
}
