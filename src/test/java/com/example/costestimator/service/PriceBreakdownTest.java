package com.example.costestimator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceBreakdownTest {

  @Test
  void totalEqualsSumOfLines() {
    PriceBreakdown b = new PriceBreakdown(List.of(
        new PriceLine("Machine time", new BigDecimal("6.00")),
        new PriceLine("Base fee", new BigDecimal("2.00"))));

    assertThat(b.total()).isEqualByComparingTo("8.00");
  }

  @Test
  void totalIsComputedNotPassedIn() {
    PriceBreakdown b = new PriceBreakdown(List.of(
        new PriceLine("Machine time", new BigDecimal("0.25")),
        new PriceLine("Minimum order", new BigDecimal("7.25"))));

    assertThat(b.total()).isEqualByComparingTo("7.50");
  }

  @Test
  void linesAreDefensivelyCopied() {
    List<PriceLine> mutable = new ArrayList<>();
    mutable.add(new PriceLine("Machine time", new BigDecimal("6.00")));

    PriceBreakdown b = new PriceBreakdown(mutable);
    mutable.add(new PriceLine("Base fee", new BigDecimal("2.00")));

    assertThat(b.lines()).hasSize(1);
    assertThat(b.total()).isEqualByComparingTo("6.00");
  }

  @Test
  void rejectsEmptyBreakdown() {
    assertThatThrownBy(() -> new PriceBreakdown(List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
