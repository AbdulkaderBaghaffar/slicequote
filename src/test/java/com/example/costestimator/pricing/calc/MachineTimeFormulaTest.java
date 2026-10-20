package com.example.costestimator.pricing.calc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class MachineTimeFormulaTest {

  private final MachineTimeFormula formula = new MachineTimeFormula();

  @Test
  void fifteenPerHourForThirtyMinutesIsSevenFifty() {
    List<PriceLine> lines = formula.price(input("30", "15.00"));

    assertThat(lines).hasSize(1);
    assertThat(lines.get(0).label()).isEqualTo("Machine time");
    assertThat(lines.get(0).amount()).isEqualByComparingTo("7.50");
  }

  @Test
  void zeroMinutesIsZero() {
    List<PriceLine> lines = formula.price(input("0", "15.00"));

    assertThat(lines).hasSize(1);
    assertThat(lines.get(0).amount()).isEqualByComparingTo("0.00");
  }

  @Test
  void roundsHalfUpAtTheBoundary() {
    // 1.00 * 0.3 / 60 = 0.005 exactly -> 0.01
    List<PriceLine> lines = formula.price(input("0.3", "1.00"));

    assertThat(lines.get(0).amount()).isEqualByComparingTo("0.01");
  }

  @Test
  void ignoresFieldsItDoesNotUse() {
    // volume, costPerGram, baseFee, minFloor all null on purpose
    assertThat(formula.price(input("30", "15.00"))).isNotEmpty();
  }

  private PricingInput input(String minutes, String rate) {
    return new PricingInput(
        new BigDecimal(minutes),
        null,
        null,
        new BigDecimal(rate),
        null,
        null);
  }
}
