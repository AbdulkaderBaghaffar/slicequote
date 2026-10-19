package com.example.costestimator.pricing.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "pricing_rate")
public class PricingRate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal hourlyRate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PricingMode mode;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal baseFee;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal minFloor;

  public PricingRate() {
  }

  public Long getId() {
    return id;
  }

  public BigDecimal getHourlyRate() {
    return hourlyRate;
  }

  public void setHourlyRate(BigDecimal hourlyRate) {
    this.hourlyRate = hourlyRate;
  }

  public PricingMode getMode() {
    return mode;
  }

  public void setMode(PricingMode mode) {
    this.mode = mode;
  }

  public BigDecimal getBaseFee() {
    return baseFee;
  }

  public void setBaseFee(BigDecimal baseFee) {
    this.baseFee = baseFee;
  }

  public BigDecimal getMinFloor() {
    return minFloor;
  }

  public void setMinFloor(BigDecimal minFloor) {
    this.minFloor = minFloor;
  }
}
