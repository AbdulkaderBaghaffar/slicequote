package com.example.costestimator.service;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.costestimator.data.Upload;
import com.example.costestimator.data.UploadStatus;
import com.example.costestimator.repository.UploadRepository;

import com.example.costestimator.pricing.PricingService;
import com.example.costestimator.pricing.calc.PriceBreakdown;

@Service
public class UploadWorker {

  private static final Logger log = LoggerFactory.getLogger(UploadWorker.class);
  private static final int MAX_ERROR_LEN = 500;
  private final UploadRepository uploadRepository;
  private final SlicerRunner slicerRunner;
  private final String storageRoot;
  private final PricingService pricingService;

  public UploadWorker(UploadRepository uploadRepository, SlicerRunner slicerRunner,
      @Value("${storage.root}") String storageRoot, PricingService pricingService) {
    this.uploadRepository = uploadRepository;
    this.slicerRunner = slicerRunner;
    this.storageRoot = storageRoot;
    this.pricingService = pricingService;

  }

  @Async
  public void process(Long uploadId) {
    try {
      Upload u = uploadRepository.findById(uploadId).orElseThrow();
      u.setStatus(UploadStatus.PROCESSING);
      uploadRepository.save(u);

      Path path = Path.of(storageRoot, u.getStoredFilename());

      SliceResult result = slicerRunner.slice(path);
      u.setPrintSeconds(result.seconds());
      u.setFilamentCm3(result.filamentCm3());
      u.setSlicerVersion(result.slicerVersion());

      PriceBreakdown bd = pricingService.priceFor(u);
      u.setEstimatedPrice(bd.total());
      u.setPriceBreakdown(bd.lines());

      u.setStatus(UploadStatus.DONE);
      uploadRepository.save(u);

    } catch (Exception e) {
      log.error("slice failed for upload {}", uploadId, e);
      uploadRepository.findById(uploadId).ifPresent(u2 -> {
        u2.setStatus(UploadStatus.FAILED);
        u2.setErrorMessage(truncate(e.getMessage()));
        uploadRepository.save(u2);
      });
    }
  }

  private String truncate(String msg) {
    if (msg == null)
      return "unknown error";
    return msg.length() > MAX_ERROR_LEN ? msg.substring(0, MAX_ERROR_LEN) : msg;
  }
}
