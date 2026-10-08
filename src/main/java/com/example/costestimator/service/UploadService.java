
package com.example.costestimator.service;

import com.example.costestimator.data.Material;
import com.example.costestimator.data.Upload;
import com.example.costestimator.data.UploadStatus;
import com.example.costestimator.dto.UploadResponse;
import com.example.costestimator.repository.MaterialRepository;
import com.example.costestimator.repository.UploadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {

  private final UploadRepository uploadRepository;
  private final MaterialRepository materialRepository;
  private final StlValidator stlValidator;
  private final String storageRoot;

  public UploadService(UploadRepository uploadRepository,
      MaterialRepository materialRepository,
      StlValidator stlValidator,
      @Value("${storage.root}") String storageRoot) {
    this.uploadRepository = uploadRepository;
    this.materialRepository = materialRepository;
    this.stlValidator = stlValidator;
    this.storageRoot = storageRoot;
  }

  public List<UploadResponse> listUploads(Long ownerId) {
    return uploadRepository.findByOwnerId(ownerId).stream()
        .map(this::toResponse)
        .toList();
  }

  public UploadResponse addUpload(MultipartFile file, Long materialId, Long ownerId) {
    if (!stlValidator.validate(file)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not a valid STL");
    }

    Material material = materialRepository.findById(materialId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown material"));

    String storedName = UUID.randomUUID() + ".stl";
    Path root = Path.of(storageRoot);
    try {
      Files.createDirectories(root);
      file.transferTo(root.resolve(storedName));
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "could not store file");
    }

    Upload upload = new Upload();
    upload.setOriginalFilename(file.getOriginalFilename());
    upload.setFilename(storedName);
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

  public UploadResponse getUpload(Long id, Long ownerId) {
    return uploadRepository.findByIdandOwnerId(id, ownerId)
        .map(this::toResponse)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
