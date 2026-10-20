package com.example.costestimator.pricing.config;

import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PricingRateSeeder implements CommandLineRunner {

  private final PricingRateRepository repo;

  public PricingRateSeeder(PricingRateRepository repo) {
    this.repo = repo;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (repo.count() == 0) {
      PricingRate rate = new PricingRate();
      rate.setHourlyRate(new BigDecimal("15.00"));
      rate.setMode(PricingMode.MACHINE_TIME);
      rate.setBaseFee(new BigDecimal("2.00"));
      rate.setMinFloor(new BigDecimal("7.50"));
      repo.save(rate);
    }
  }
}
