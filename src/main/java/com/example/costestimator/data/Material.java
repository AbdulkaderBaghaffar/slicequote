package com.example.costestimator.data;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // creates table
public class Material {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private String name;
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal costPerGram;

  public Material() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal getCostPerGram() {
    return costPerGram;
  }

  public void setCostPerGram(BigDecimal costPerGram) {
    this.costPerGram = costPerGram;
  }

}
