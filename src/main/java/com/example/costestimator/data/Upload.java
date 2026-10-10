package com.example.costestimator.data;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Upload {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String originalFilename;
  private UploadStatus status = UploadStatus.PENDING;
  private String errorMessage;

  @ManyToOne
  private Material material;

  private BigDecimal estimatedPrice;
  private Instant createdAt;
  private Long ownerId;
  private String storedFilename;

  public Upload() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getOriginalFilename() {
    return originalFilename;
  }

  public void setOriginalFilename(String originalFilename) {
    this.originalFilename = originalFilename;
  }

  public UploadStatus getStatus() {
    return status;
  }

  public void setStatus(UploadStatus status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Material getMaterial() {
    return material;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public BigDecimal getEstimatedPrice() {
    return estimatedPrice;
  }

  public void setEstimatedPrice(BigDecimal estimatedPrice) {
    this.estimatedPrice = estimatedPrice;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Long ownerId) {
    this.ownerId = ownerId;
  }

  public String getFilename() {
    return storedFilename;
  }

  public void setFilename(String storedFilename) {
    this.storedFilename = storedFilename;
  }
}
