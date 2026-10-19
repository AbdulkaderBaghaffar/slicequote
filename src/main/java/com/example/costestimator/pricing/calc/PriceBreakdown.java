package com.example.costestimator.pricing.calc;

import java.math.BigDecimal;
import java.util.List;

public record PriceBreakdown(BigDecimal total, List<PriceLine> lines) {

  public PriceBreakdown(List<PriceLine> lines) {
    this(requireSum(lines), List.copyOf(lines));
  }

  private static BigDecimal requireSum(List<PriceLine> lines) {
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("breakdown has no lines");
    }
    return lines.stream()
        .map(PriceLine::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
