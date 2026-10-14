package com.example.costestimator.service;

import com.example.costestimator.data.Upload;
import com.example.costestimator.data.UploadStatus;
import com.example.costestimator.repository.UploadRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupRecovery {

  private final UploadRepository repo;
  private final Logger log = LoggerFactory.getLogger(getClass());

  public StartupRecovery(UploadRepository repo) {
    this.repo = repo;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void failStuckUploads() {
    List<Upload> stuck = repo.findByStatus(UploadStatus.PROCESSING);
    stuck.forEach(u -> {
      u.setStatus(UploadStatus.FAILED);
      u.setErrorMessage("interrupted by restart");
    });
    repo.saveAll(stuck);
    log.warn("marked {} stuck uploads as FAILED", stuck.size());
  }
}
