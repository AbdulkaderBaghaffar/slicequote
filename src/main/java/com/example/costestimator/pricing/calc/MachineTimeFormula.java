package com.example.costestimator.pricing.calc;

import java.math.BigDecimal;
import java.util.List;

public class MachineTimeFormula {
  public List<PriceLine> price(PricingInput in) {
    BigDecimal amount = Money.round(
        Money.divide(in.hourlyRate().multiply(in.minutes()),
            BigDecimal.valueOf(60)));
    return List.of(new PriceLine("Machine time", amount));
  }
}
