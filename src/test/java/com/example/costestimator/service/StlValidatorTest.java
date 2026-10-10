package com.example.costestimator.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class StlValidatorTest {

  private final StlValidator validator = new StlValidator();

  @Test
  void acceptsBinaryStl() {
    byte[] bytes = new byte[84 + 50 * 40]; // 2084
    assertTrue(validator.validate(file(bytes)));
  }

  @Test
  void rejectsWrongSize() {
    byte[] bytes = new byte[2000]; // (2000-84) % 50 != 0
    assertFalse(validator.validate(file(bytes)));
  }

  @Test
  void acceptsAsciiStl() {
    byte[] bytes = new byte[100];
    byte[] prefix = "solid".getBytes(StandardCharsets.US_ASCII);
    System.arraycopy(prefix, 0, bytes, 0, prefix.length);
    assertTrue(validator.validate(file(bytes)));
  }

  @Test
  void rejectsTooSmall() {
    assertFalse(validator.validate(file(new byte[83])));
  }

  private MockMultipartFile file(byte[] bytes) {
    return new MockMultipartFile("file", "part.stl", null, bytes);
  }
}
