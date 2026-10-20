package com.example.costestimator.pricing.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void roundsHalfUpAtBoundary() {
    assertThat(Money.round(new BigDecimal("6.005"))).isEqualByComparingTo("6.01");
  }

  @Test
  void roundsDownBelowBoundary() {
    assertThat(Money.round(new BigDecimal("6.004"))).isEqualByComparingTo("6.00");
  }

  @Test
  void roundsNegativeHalfUpAwayFromZero() {
    assertThat(Money.round(new BigDecimal("-6.005"))).isEqualByComparingTo("-6.01");
  }

  @Test
  void alwaysProducesTwoDecimalPlaces() {
    assertThat(Money.round(new BigDecimal("7.5")).scale()).isEqualTo(2);
    assertThat(Money.round(new BigDecimal("7")).scale()).isEqualTo(2);
  }

  @Test
  void divideDoesNotThrowOnNonTerminatingResult() {
    assertThatCode(() -> Money.divide(new BigDecimal("1000"), BigDecimal.valueOf(60)))
        .doesNotThrowAnyException();
  }

  @Test
  void divideKeepsIntermediatePrecision() {
    BigDecimal result = Money.divide(new BigDecimal("1000"), BigDecimal.valueOf(60));
    assertThat(result.scale()).isGreaterThan(Money.SCALE);
  }
}
