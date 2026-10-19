package com.example.costestimator.service;

import com.example.costestimator.data.PricingMode;
import java.util.List;

public class PricingDispatcher {

  private final MachineTimeFormula machineTime;

  public PricingDispatcher(MachineTimeFormula machineTime) {
    this.machineTime = machineTime;
  }

  public List<PriceLine> price(PricingMode mode, PricingInput input) {
    return switch (mode) {
      case MACHINE_TIME -> machineTime.price(input);
    };
  }
}
