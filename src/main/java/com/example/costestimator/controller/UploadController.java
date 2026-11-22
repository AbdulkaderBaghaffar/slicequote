package com.example.costestimator.controller;

import com.example.costestimator.dto.UploadResponse;
import com.example.costestimator.repository.UserRepository;
import com.example.costestimator.service.UploadService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController // defines class as HTTP request and retuns method output as json
public class UploadController {

  private static final String GUEST_HEADER = "X-Guest-Id";
  private static final int GUEST_KEY_MAX_LENGTH = 64;

  private final UploadService uploadService;
  private final UserRepository userRepository;

  public UploadController(UploadService uploadService, UserRepository userRepository) {
    this.uploadService = uploadService;
    this.userRepository = userRepository;

  }

  private Long currentOwnerId(Authentication auth) {
    if (auth == null || auth instanceof AnonymousAuthenticationToken
        || "anonymousUser".equals(auth.getName())) {
      return null;
    }
    return userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
        .getId();
  }

  private static String guestKeyOrNull(String guestKey) {
    if (guestKey == null) return null;
    String trimmed = guestKey.trim();
    if (trimmed.isEmpty()) return null;
    if (trimmed.length() > GUEST_KEY_MAX_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "guest id is too long");
    }
    return trimmed;
  }

  private static void requireAccess(Long ownerId, String guestKey) {
    if (ownerId == null && guestKey == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
          "Authentication or guest id required");
    }
  }

  @GetMapping("/uploads")
  public List<UploadResponse> listUploads(Authentication auth) {
    Long ownerId = currentOwnerId(auth);
    if (ownerId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }
    return uploadService.listUploads(ownerId);
  }

  @GetMapping("/uploads/{id}")
  public UploadResponse getUpload(
      @PathVariable Long id,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey,
      Authentication auth) {
    Long ownerId = currentOwnerId(auth);
    String key = guestKeyOrNull(guestKey);
    requireAccess(ownerId, key);
    return uploadService.getUpload(id, ownerId, key);
  }

  @PostMapping("/uploads")
  @ResponseStatus(HttpStatus.CREATED)
  public UploadResponse addUpload(
      @RequestParam("file") MultipartFile file,
      @RequestParam Long materialId,
      @RequestParam(defaultValue = "1") int quantity,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey,
      Authentication auth) {
    Long ownerId = currentOwnerId(auth);
    String key = guestKeyOrNull(guestKey);
    requireAccess(ownerId, key);
    return uploadService.addUpload(file, materialId, quantity, ownerId, key);
  }

  @PostMapping("/uploads/{id}/reprice")
  public UploadResponse repriceUpload(
      @PathVariable Long id,
      @RequestParam(defaultValue = "1") int quantity,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey,
      Authentication auth) {
    Long ownerId = currentOwnerId(auth);
    String key = guestKeyOrNull(guestKey);
    requireAccess(ownerId, key);
    return uploadService.reprice(id, quantity, ownerId, key);
  }
}
