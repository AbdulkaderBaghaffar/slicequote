package com.example.costestimator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.example.costestimator.data.Upload;
import com.example.costestimator.repository.UploadRepository;
import com.example.costestimator.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController // defines class as HTTP request and retuns method output as json
public class UploadController {

  private final UploadRepository uploadRepository;
  private final UserRepository userRepository;

  public UploadController(UploadRepository uploadRepository, UserRepository userRepository) {
    this.uploadRepository = uploadRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/uploads") // when get request at materials return this
  public List<Upload> listUploads() {
    return uploadRepository.findAll();

  }

  @PostMapping("/uploads")
  @ResponseStatus(HttpStatus.CREATED)
  public Upload addUpload(@RequestBody Upload upload, Authentication authentication) { // return type material from JSON
                                                                                       // made from Upload
    // object

    upload.setEstimatedPrice(new BigDecimal("10.00")); // tmp
    upload.setCreatedAt(Instant.now());
    Long ownerId = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED))
        .getId();
    upload.setOwnerId(ownerId);
    ;
    return uploadRepository.save(upload);
  }
}
