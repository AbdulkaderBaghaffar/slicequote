package com.example.costestimator.service;

import java.math.BigDecimal;

public record PriceLine(String label, BigDecimal amount) {
}
