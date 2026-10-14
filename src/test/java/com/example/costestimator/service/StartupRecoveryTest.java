package com.example.costestimator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.costestimator.data.Upload;
import com.example.costestimator.data.UploadStatus;
import com.example.costestimator.repository.UploadRepository;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StartupRecoveryTest {

  @Autowired
  UploadRepository repo;
  @Autowired
  StartupRecovery recovery;

  @Test
  void marksStuckProcessingUploadsAsFailed() {
    Upload u = new Upload();
    u.setOriginalFilename("a.stl");
    u.setStoredFilename("a.stl");
    u.setStatus(UploadStatus.PROCESSING);
    u.setOwnerId(1L);
    u.setCreatedAt(Instant.now());
    Long id = repo.save(u).getId();

    recovery.failStuckUploads();

    Upload after = repo.findById(id).orElseThrow();
    assertEquals(UploadStatus.FAILED, after.getStatus());
    assertEquals("interrupted by restart", after.getErrorMessage());
  }
}
