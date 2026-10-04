package com.example.costestimator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.costestimator.data.Material;
import com.example.costestimator.data.Upload;
import com.example.costestimator.repository.MaterialRepository;
import com.example.costestimator.repository.UploadRepository;
import com.example.costestimator.repository.UserRepository;
import com.example.costestimator.dto.UploadRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController // defines class as HTTP request and retuns method output as json
public class UploadController {

  private final UploadRepository uploadRepository;
  private final UserRepository userRepository;
  private final MaterialRepository materialRepository;

  public UploadController(UploadRepository uploadRepository, UserRepository userRepository,
      MaterialRepository materialRepository) {
    this.uploadRepository = uploadRepository;
    this.userRepository = userRepository;
    this.materialRepository = materialRepository;
  }

  @GetMapping("/uploads") // when get request at materials return this
  public List<Upload> listUploads(Authentication authentication) {
    Long ownerId = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
        .getId();

    return uploadRepository.findByOwnerId(ownerId);

  }

  @PostMapping("/uploads")
  @ResponseStatus(HttpStatus.CREATED)
  public Upload addUpload(@RequestBody UploadRequest request, Authentication authentication) {

    Material material = materialRepository.findById(request.materialId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    Long ownerId = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
        .getId();

    Upload upload = new Upload();
    upload.setFilename(request.filename());
    upload.setMaterial(material);
    upload.setOwnerId(ownerId);
    upload.setEstimatedPrice(new BigDecimal("10.00"));
    upload.setCreatedAt(Instant.now());

    return uploadRepository.save(upload);
  }
}
