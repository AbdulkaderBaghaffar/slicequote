package com.example.costestimator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

  public static final int SCALE = 2;

  private static final int INTERMEDIATE_SCALE = SCALE + 6;

  private Money() {
  }

  public static BigDecimal round(BigDecimal value) {
    return value.setScale(SCALE, RoundingMode.HALF_UP);
  }

  public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
    return numerator.divide(denominator, INTERMEDIATE_SCALE, RoundingMode.HALF_UP);
  }
}
