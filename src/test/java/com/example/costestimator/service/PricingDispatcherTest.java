package com.example.costestimator.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.costestimator.data.PricingMode;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingDispatcherTest {

  private final PricingDispatcher dispatcher = new PricingDispatcher(new MachineTimeFormula());

  @Test
  void machineTimeModeRoutesToMachineTimeFormula() {
    List<PriceLine> out = dispatcher.price(PricingMode.MACHINE_TIME, input("30", "15.00"));

    assertThat(out).hasSize(1);
    assertThat(out.get(0).label()).isEqualTo("Machine time");
    assertThat(out.get(0).amount()).isEqualByComparingTo("7.50");
  }

  private PricingInput input(String minutes, String rate) {
    return new PricingInput(new BigDecimal(minutes), null, null, new BigDecimal(rate), null, null);
  }
}
