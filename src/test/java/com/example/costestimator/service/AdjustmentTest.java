package com.example.costestimator.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdjustmentTest {

  private final Adjustment adjustment = new Adjustment();

  @Test
  void baseFeeIsAddedAndFloorDoesNotBite() {
    List<PriceLine> out = adjustment.adjust(lines("Machine time", "6.00"), bd("2.00"), bd("7.50"));

    assertThat(out).hasSize(2);
    assertThat(out.get(1).label()).isEqualTo("Base fee");
    assertThat(out.get(1).amount()).isEqualByComparingTo("2.00");
    assertThat(total(out)).isEqualByComparingTo("8.00");
  }

  @Test
  void floorLineAppearsAsTheDelta() {
    List<PriceLine> out = adjustment.adjust(lines("Machine time", "6.00"), bd("0.00"), bd("7.50"));

    assertThat(out).hasSize(2);
    assertThat(out.get(1).label()).isEqualTo("Minimum order");
    assertThat(out.get(1).amount()).isEqualByComparingTo("1.50");
    assertThat(total(out)).isEqualByComparingTo("7.50");
  }

  @Test
  void floorLineAbsentWhenItDoesNotBite() {
    List<PriceLine> out = adjustment.adjust(lines("Machine time", "20.00"), bd("2.00"), bd("7.50"));

    assertThat(out).hasSize(2);
    assertThat(out).extracting(PriceLine::label).containsExactly("Machine time", "Base fee");
    assertThat(total(out)).isEqualByComparingTo("22.00");
  }

  @Test
  void zeroBaseFeeProducesNoLine() {
    List<PriceLine> out = adjustment.adjust(lines("Machine time", "20.00"), bd("0.00"), bd("7.50"));

    assertThat(out).hasSize(1);
    assertThat(out.get(0).label()).isEqualTo("Machine time");
  }

  @Test
  void floorIsCheckedAfterBaseFee() {
    // subtotal 7.00 + fee 2.00 = 9.00 >= 7.50 -> no floor line
    // (if floor were applied to the subtotal first, total would be 9.50)
    List<PriceLine> out = adjustment.adjust(lines("Machine time", "7.00"), bd("2.00"), bd("7.50"));

    assertThat(out).hasSize(2);
    assertThat(total(out)).isEqualByComparingTo("9.00");
  }

  @Test
  void doesNotMutateTheInputList() {
    List<PriceLine> original = lines("Machine time", "6.00");

    adjustment.adjust(original, bd("2.00"), bd("7.50"));

    assertThat(original).hasSize(1);
  }

  @Test
  void acceptsImmutableInputList() {
    assertThat(adjustment.adjust(List.of(new PriceLine("Machine time", bd("6.00"))), bd("2.00"), bd("7.50")))
        .hasSize(2);
  }

  private List<PriceLine> lines(String label, String amount) {
    return List.of(new PriceLine(label, bd(amount)));
  }

  private BigDecimal bd(String v) {
    return new BigDecimal(v);
  }

  private BigDecimal total(List<PriceLine> lines) {
    return lines.stream().map(PriceLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
