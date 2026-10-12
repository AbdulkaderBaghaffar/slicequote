package com.example.costestimator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.costestimator.data.Upload;
import com.example.costestimator.data.UploadStatus;
import com.example.costestimator.repository.UploadRepository;

@Service
public class UploadWorker {

  private static final Logger log = LoggerFactory.getLogger(UploadWorker.class);
  private static final int MAX_ERROR_LEN = 500;

  private final UploadRepository uploadRepository;

  public UploadWorker(UploadRepository uploadRepository) {
    this.uploadRepository = uploadRepository;
  }

  @Async
  public void process(Long uploadId) {
    try {
      Upload u = uploadRepository.findById(uploadId).orElseThrow();
      u.setStatus(UploadStatus.PROCESSING);
      uploadRepository.save(u);

      Thread.sleep(3000); // TODO: replace with slicerRunner.slice(path)

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
