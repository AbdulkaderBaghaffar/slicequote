package com.example.costestimator.controller;

import com.example.costestimator.dto.UploadResponse;
import com.example.costestimator.repository.UserRepository;
import com.example.costestimator.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController // defines class as HTTP request and retuns method output as json
public class UploadController {

  private final UploadService uploadService;
  private final UserRepository userRepository;

  public UploadController(UploadService uploadService, UserRepository userRepository) {
    this.uploadService = uploadService;
    this.userRepository = userRepository;

  }

  private Long currentOwnerId(Authentication auth) {
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
        .getId();
  }

  @GetMapping("/uploads") // when get request at materials return this
  public List<UploadResponse> listUploads(Authentication auth) {
    return uploadService.listUploads(currentOwnerId(auth));
  }

  @GetMapping("/uploads/{id}")
  public UploadResponse getUpload(@PathVariable Long id, Authentication auth) {
    return uploadService.getUpload(id, currentOwnerId(auth));
  }

  @PostMapping("/uploads")
  @ResponseStatus(HttpStatus.CREATED)
  public UploadResponse addUpload(@RequestParam("file") MultipartFile file, @RequestParam Long materialId,
      Authentication auth) {
    return uploadService.addUpload(file, materialId, currentOwnerId(auth));
  }
}
