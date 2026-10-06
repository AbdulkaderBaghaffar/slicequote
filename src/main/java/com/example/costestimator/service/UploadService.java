package com.example.costestimator.service;

import com.example.costestimator.data.Material;
import com.example.costestimator.data.Upload;
import com.example.costestimator.data.UploadStatus;
import com.example.costestimator.dto.UploadResponse;
import com.example.costestimator.repository.MaterialRepository;
import com.example.costestimator.repository.UploadRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class UploadService {

  private final UploadRepository uploadRepository;
  private final MaterialRepository materialRepository;

  public UploadService(UploadRepository uploadRepository, MaterialRepository materialRepository) {
    this.uploadRepository = uploadRepository;
    this.materialRepository = materialRepository;
  }

  public List<UploadResponse> listUploads(Long ownerId) {
    return uploadRepository.findByOwnerId(ownerId).stream()
        .map(this::toResponse)
        .toList();
  }

  public UploadResponse addUpload(MultipartFile file, Long materialId, Long ownerId) {
    Material material = materialRepository.findById(materialId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

    Upload upload = new Upload();
    upload.setOriginalFilename(file.getOriginalFilename());
    upload.setStatus(UploadStatus.PENDING);
    upload.setMaterial(material);
    upload.setOwnerId(ownerId);
    upload.setCreatedAt(Instant.now());

    return toResponse(uploadRepository.save(upload));
  }

  private UploadResponse toResponse(Upload u) {
    return new UploadResponse(
        u.getId(),
        u.getOriginalFilename(),
        u.getMaterial().getId(),
        u.getStatus(),
        u.getEstimatedPrice(),
        u.getErrorMessage(),
        u.getCreatedAt());
  }
}
