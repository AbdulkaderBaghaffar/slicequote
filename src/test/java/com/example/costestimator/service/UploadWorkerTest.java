package com.example.costestimator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.costestimator.data.Material;
import com.example.costestimator.data.Upload;
import com.example.costestimator.data.UploadStatus;
import com.example.costestimator.pricing.PricingService;
import com.example.costestimator.pricing.calc.PriceLine;
import com.example.costestimator.pricing.config.PricingMode;
import com.example.costestimator.pricing.config.PricingRate;
import com.example.costestimator.pricing.config.PricingRateRepository;
import com.example.costestimator.pricing.config.PricingRateService;
import com.example.costestimator.repository.MaterialRepository;
import com.example.costestimator.repository.UploadRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest(properties = "storage.root=/tmp/slicequote-test")
@Import({ UploadWorker.class, PricingService.class, PricingRateService.class })
class UploadWorkerTest {

  @MockitoBean
  private SlicerRunner slicerRunner;

  @Autowired
  private UploadWorker worker;
  @Autowired
  private PricingService pricingService;
  @Autowired
  private UploadRepository uploadRepository;
  @Autowired
  private MaterialRepository materialRepository;
  @Autowired
  private PricingRateRepository pricingRateRepository;

  private Long uploadId;

  @BeforeEach
  void setUp() {
    pricingRateRepository.deleteAll();
    PricingRate rate = new PricingRate();
    rate.setHourlyRate(new BigDecimal("15.00"));
    rate.setMode(PricingMode.MACHINE_TIME);
    rate.setBaseFee(new BigDecimal("2.00"));
    rate.setMinFloor(new BigDecimal("7.50"));
    pricingRateRepository.saveAndFlush(rate);

    Material m = new Material();
    m.setName("PLA");
    m.setCostPerGram(new BigDecimal("0.05"));
    materialRepository.saveAndFlush(m);

    Upload u = new Upload();
    u.setOriginalFilename("cube.stl");
    u.setStoredFilename("test.stl");
    u.setStatus(UploadStatus.PENDING);
    u.setMaterial(m);
    u.setOwnerId(1L);
    u.setCreatedAt(Instant.now());
    uploadId = uploadRepository.saveAndFlush(u).getId();
  }

  @Test
  void happyPathSetsDonePriceAndBreakdown() {
    when(slicerRunner.slice(any(Path.class)))
        .thenReturn(new SliceResult(1800L, new BigDecimal("3.59"), "2.7.0"));

    worker.process(uploadId);

    Upload u = uploadRepository.findById(uploadId).orElseThrow();
    assertThat(u.getStatus()).isEqualTo(UploadStatus.DONE);
    assertThat(u.getErrorMessage()).isNull();
    assertThat(u.getEstimatedPrice()).isNotNull();

    // 1800s = 30min -> 15.00 * 30 / 60 = 7.50 ; + 2.00 base fee = 9.50 ; floor 7.50
    // does not bite
    assertThat(u.getEstimatedPrice()).isEqualByComparingTo("9.50");
    assertThat(u.getPriceBreakdown()).hasSize(2);
    assertThat(u.getPriceBreakdown().get(0).label()).isEqualTo("Machine time");
    assertThat(u.getPriceBreakdown().get(0).amount()).isEqualByComparingTo("7.50");
    assertThat(u.getPriceBreakdown().get(1).label()).isEqualTo("Base fee");
  }

  @Test
  void breakdownTotalEqualsSumOfLinesAfterReload() {
    when(slicerRunner.slice(any(Path.class)))
        .thenReturn(new SliceResult(1800L, new BigDecimal("3.59"), "2.7.0"));

    worker.process(uploadId);

    Upload u = uploadRepository.findById(uploadId).orElseThrow();
    BigDecimal sum = u.getPriceBreakdown().stream()
        .map(PriceLine::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertThat(sum).isEqualByComparingTo(u.getEstimatedPrice());
  }

  @Test
  void nullPrintSecondsFailsCleanly() {
    Upload u = uploadRepository.findById(uploadId).orElseThrow();
    u.setPrintSeconds(null);

    assertThatThrownBy(() -> pricingService.priceFor(u))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("print time");
  }

  @Test
  void zeroPrintSecondsFailsCleanly() {
    when(slicerRunner.slice(any(Path.class)))
        .thenReturn(new SliceResult(0L, null, null));

    worker.process(uploadId);

    Upload u = uploadRepository.findById(uploadId).orElseThrow();
    assertThat(u.getStatus()).isEqualTo(UploadStatus.FAILED);
    assertThat(u.getEstimatedPrice()).isNull();
  }

  @Test
  void slicerFailureLeavesFailedNotEmptyDone() {
    when(slicerRunner.slice(any(Path.class)))
        .thenThrow(new RuntimeException("slicer exploded"));

    worker.process(uploadId);

    Upload u = uploadRepository.findById(uploadId).orElseThrow();
    assertThat(u.getStatus()).isEqualTo(UploadStatus.FAILED);
    assertThat(u.getErrorMessage()).contains("slicer exploded");
    assertThat(u.getEstimatedPrice()).isNull();
  }

  @Test
  void pricingFailureLeavesFailedNotStuckInProcessing() {
    when(slicerRunner.slice(any(Path.class)))
        .thenReturn(new SliceResult(1800L, new BigDecimal("3.59"), "2.7.0"));
    pricingRateRepository.deleteAll(); // no rate row -> PricingRateService throws

    worker.process(uploadId);

    Upload u = uploadRepository.findById(uploadId).orElseThrow();
    assertThat(u.getStatus()).isEqualTo(UploadStatus.FAILED);
    assertThat(u.getErrorMessage()).contains("pricing_rate");
    assertThat(u.getEstimatedPrice()).isNull();
  }
}
