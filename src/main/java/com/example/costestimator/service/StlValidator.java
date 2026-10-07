package com.example.costestimator.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class StlValidator {

  public boolean validate(MultipartFile file) {
    long size = file.getSize();

    if (size < 84) {
      return false;
    }
    if ((size - 84) % 50 == 0) {
      return true; // binary STL
    }

    byte[] header;
    try {
      header = file.getInputStream().readNBytes(5);
    } catch (IOException e) {
      return false; // unreadable -> reject
    }

    return new String(header, StandardCharsets.US_ASCII).equals("solid");
  }
}
