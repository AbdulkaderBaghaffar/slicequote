package com.example.costestimator.service;

import com.example.costestimator.data.PricingRate;
import com.example.costestimator.repository.PricingRateRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PricingRateService {

  private static final Logger log = LoggerFactory.getLogger(PricingRateService.class);

  private final PricingRateRepository repo;

  public PricingRateService(PricingRateRepository repo) {
    this.repo = repo;
  }

  public PricingRate getActiveRate() {
    List<PricingRate> rows = repo.findAll();
    if (rows.size() != 1) {
      throw new IllegalStateException("Expected exactly 1 pricing_rate row but found " + rows.size());
    }
    return rows.get(0);
  }

  public void logActive() {
    PricingRate rate = getActiveRate();
    log.info("Pricing active — mode: {} | rate: {}/hr", rate.getMode(), rate.getHourlyRate());
  }
}
