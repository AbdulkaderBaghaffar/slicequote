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

  private Long printSeconds;
  private BigDecimal filamentCm3;
  private String slicerVersion;

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

  public Long getPrintSeconds() {
    return printSeconds;
  }

  public void setPrintSeconds(Long printSeconds) {
    this.printSeconds = printSeconds;
  }

  public BigDecimal getFilamentCm3() {
    return filamentCm3;
  }

  public void setFilamentCm3(BigDecimal filamentCm3) {
    this.filamentCm3 = filamentCm3;
  }

  public String getSlicerVersion() {
    return slicerVersion;
  }

  public void setSlicerVersion(String slicerVersion) {
    this.slicerVersion = slicerVersion;
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

  public String getStoredFilename() {
    return storedFilename;
  }

  public void setStoredFilename(String storedFilename) {
    this.storedFilename = storedFilename;
  }
}
