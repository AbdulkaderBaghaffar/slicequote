
package com.example.costestimator.pricing.calc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Adjustment {

  public List<PriceLine> adjust(
      List<PriceLine> lines, BigDecimal baseFee, BigDecimal minFloor) {

    List<PriceLine> out = new ArrayList<>(lines);

    BigDecimal subtotal = lines.stream()
        .map(PriceLine::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal total = subtotal;

    if (baseFee != null && baseFee.signum() > 0) {
      BigDecimal fee = Money.round(baseFee);
      out.add(new PriceLine("Base fee", fee));
      total = total.add(fee);
    }

    if (minFloor != null && total.compareTo(minFloor) < 0) {
      out.add(new PriceLine("Minimum order", Money.round(minFloor.subtract(total))));
    }

    return out;
  }
}
