package com.example.costestimator.pricing;

import com.example.costestimator.data.Upload;
import com.example.costestimator.pricing.calc.Adjustment;
import com.example.costestimator.pricing.calc.MachineTimeFormula;
import com.example.costestimator.pricing.calc.Money;
import com.example.costestimator.pricing.calc.PriceBreakdown;
import com.example.costestimator.pricing.calc.PricingDispatcher;
import com.example.costestimator.pricing.calc.PricingInput;
import com.example.costestimator.pricing.config.PricingRate;
import com.example.costestimator.pricing.config.PricingRateService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

  private final PricingRateService rateService;
  private final PricingDispatcher dispatcher;
  private final Adjustment adjustment;

  public PricingService(PricingRateService rateService) {
    this.rateService = rateService;
    this.dispatcher = new PricingDispatcher(new MachineTimeFormula());
    this.adjustment = new Adjustment();
  }

  public PriceBreakdown priceFor(Upload upload) {
    PricingRate rate = rateService.getActiveRate();

    Long secs = upload.getPrintSeconds();
    if (secs == null || secs <= 0) {
      throw new IllegalStateException(
          "upload " + upload.getId() + " has no print time (printSeconds=" + secs + ")");
    }

    BigDecimal minutes = Money.divide(
        BigDecimal.valueOf(secs),
        BigDecimal.valueOf(60));

    PricingInput input = new PricingInput(
        minutes,
        upload.getFilamentCm3(),
        null, // costPerGram — unused by MACHINE_TIME
        rate.getHourlyRate(),
        rate.getBaseFee(),
        rate.getMinFloor());

    var lines = dispatcher.price(rate.getMode(), input);
    return new PriceBreakdown(adjustment.adjust(lines, rate.getBaseFee(), rate.getMinFloor()));
  }
}
