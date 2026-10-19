package com.example.costestimator.pricing.calc;

import java.math.BigDecimal;

public record PricingInput(
    BigDecimal minutes,
    BigDecimal volumeCm3,
    BigDecimal costPerGram,
    BigDecimal hourlyRate,
    BigDecimal baseFee,
    BigDecimal minFloor) {
}
