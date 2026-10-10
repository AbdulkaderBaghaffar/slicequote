package com.example.costestimator.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.costestimator.data.Material;
import com.example.costestimator.repository.MaterialRepository;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UploadControllerTest {

  @Autowired
  MaterialRepository materialRepository;
  @Value("${local.server.port}")
  int port;
  @Value("${storage.root}")
  String storageRoot;

  private final HttpClient http = HttpClient.newHttpClient();
  private static final String BOUNDARY = "----slicequotetest";
  private Long materialId;

  @BeforeEach
  void seedMaterial() {
    Material m = new Material();
    m.setName("PLA");
    m.setCostPerGram(new BigDecimal("0.05"));
    materialId = materialRepository.save(m).getId();
  }

  @Test
  void happyPathStoresFileAndReturnsPending() throws Exception {
    long before = countStoredFiles();

    HttpResponse<String> res = postUpload(materialId, binaryStl(4), "part.stl", token("a@test.com"));

    assertEquals(HttpStatus.CREATED.value(), res.statusCode());
    assertEquals("PENDING", jsonString(res.body(), "status"));
    assertEquals("part.stl", jsonString(res.body(), "originalFilename"));
    assertTrue(res.body().contains("\"estimatedPrice\":null"));
    assertEquals(before + 1, countStoredFiles(), "file should land on disk");
  }

  @Test
  void oversizedFileReturns413() throws Exception {
    HttpResponse<String> res = postUpload(materialId, new byte[1500], "big.stl", token("a@test.com"));
    assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.value(), res.statusCode());
  }

  @Test
  void nonStlReturns400() throws Exception {
    byte[] junk = "hello world not a model".getBytes(StandardCharsets.US_ASCII);
    HttpResponse<String> res = postUpload(materialId, junk, "note.stl", token("a@test.com"));
    assertEquals(HttpStatus.BAD_REQUEST.value(), res.statusCode());
  }

  @Test
  void unknownMaterialReturns400() throws Exception {
    HttpResponse<String> res = postUpload(999999L, binaryStl(4), "part.stl", token("a@test.com"));
    assertEquals(HttpStatus.BAD_REQUEST.value(), res.statusCode());
  }

  @Test
  void missingFilePartReturns400() throws Exception {
    HttpResponse<String> res = postUpload(materialId, null, null, token("a@test.com"));
    assertEquals(HttpStatus.BAD_REQUEST.value(), res.statusCode());
  }

  @Test
  void otherUsersUploadReturns404() throws Exception {
    HttpResponse<String> created = postUpload(materialId, binaryStl(4), "part.stl", token("a@test.com"));
    long id = jsonNumber(created.body(), "id");

    HttpResponse<String> res = send("GET", "/uploads/" + id, token("b@test.com"), null, null);
    assertEquals(HttpStatus.NOT_FOUND.value(), res.statusCode());
  }

  // ---------- helpers ----------

  private byte[] binaryStl(int triangles) {
    return new byte[84 + 50 * triangles];
  }

  private String token(String email) throws Exception {
    String creds = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
    send("POST", "/auth/signup", null, creds.getBytes(StandardCharsets.UTF_8), "application/json");
    HttpResponse<String> login = send("POST", "/auth/login", null, creds.getBytes(StandardCharsets.UTF_8),
        "application/json");
    assertEquals(HttpStatus.OK.value(), login.statusCode(), "login failed: " + login.body());
    return jsonString(login.body(), "token");
  }

  private HttpResponse<String> postUpload(Long materialId, byte[] file, String filename, String token)
      throws Exception {
    return send("POST", "/uploads", token, multipart(materialId, file, filename),
        "multipart/form-data; boundary=" + BOUNDARY);
  }

  private HttpResponse<String> send(String method, String path, String token, byte[] body,
      String contentType) throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    if (token != null) {
      b.header("Authorization", "Bearer " + token);
    }
    if (body != null) {
      b.header("Content-Type", contentType);
      b.method(method, HttpRequest.BodyPublishers.ofByteArray(body));
    } else {
      b.method(method, HttpRequest.BodyPublishers.noBody());
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  private byte[] multipart(Long materialId, byte[] file, String filename) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    if (materialId != null) {
      part(out, "materialId", null, materialId.toString().getBytes(StandardCharsets.UTF_8));
    }
    if (file != null) {
      part(out, "file", filename, file);
    }
    raw(out, "--" + BOUNDARY + "--\r\n");
    return out.toByteArray();
  }

  private void part(ByteArrayOutputStream out, String name, String filename, byte[] value)
      throws Exception {
    raw(out, "--" + BOUNDARY + "\r\n");
    raw(out, "Content-Disposition: form-data; name=\"" + name + "\""
        + (filename == null ? "" : "; filename=\"" + filename + "\"") + "\r\n");
    if (filename != null) {
      raw(out, "Content-Type: application/octet-stream\r\n");
    }
    raw(out, "\r\n");
    out.write(value);
    raw(out, "\r\n");
  }

  private void raw(ByteArrayOutputStream out, String s) throws Exception {
    out.write(s.getBytes(StandardCharsets.UTF_8));
  }

  private long countStoredFiles() throws Exception {
    Path root = Path.of(storageRoot);
    if (!Files.exists(root)) {
      return 0;
    }
    try (var files = Files.list(root)) {
      return files.count();
    }
  }

  private static String jsonString(String body, String field) {
    Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(body);
    assertTrue(m.find(), field + " not found in: " + body);
    return m.group(1);
  }

  private static long jsonNumber(String body, String field) {
    Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*(-?\\d+)").matcher(body);
    assertTrue(m.find(), field + " not found in: " + body);
    return Long.parseLong(m.group(1));
  }
}
