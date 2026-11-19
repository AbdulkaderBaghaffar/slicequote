package com.example.costestimator.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

  @Value("${local.server.port}")
  int port;

  private final HttpClient http = HttpClient.newHttpClient();

  @Test
  void signupReturnsTokenThatCanAccessProtectedRoutes() throws Exception {
    HttpResponse<String> res = post("/auth/signup", credentials("signup@test.com", "password123"));

    assertEquals(201, res.statusCode());
    String token = jsonString(res.body(), "token");
    assertFalse(token.isBlank());

    HttpResponse<String> materials = send("GET", "/materials", token, null, null);
    assertEquals(200, materials.statusCode());
  }

  @Test
  void signupNormalizesEmailForLogin() throws Exception {
    assertEquals(201,
        post("/auth/signup", credentials("Case.User@test.com", "password123")).statusCode());

    HttpResponse<String> login = post("/auth/login",
        credentials("case.user@test.com", "password123"));

    assertEquals(200, login.statusCode());
    assertFalse(jsonString(login.body(), "token").isBlank());
  }

  @Test
  void duplicateEmailReturns409OnEmailField() throws Exception {
    String body = credentials("duplicate@test.com", "password123");
    assertEquals(201, post("/auth/signup", body).statusCode());

    HttpResponse<String> res = post("/auth/signup", body);

    assertEquals(409, res.statusCode());
    assertEquals("Email already registered", jsonString(res.body(), "message"));
    assertEquals("email", jsonString(res.body(), "field"));
  }

  @Test
  void invalidEmailReturns400OnEmailField() throws Exception {
    HttpResponse<String> res = post("/auth/signup", credentials("not-an-email", "password123"));

    assertEquals(400, res.statusCode());
    assertEquals("email", jsonString(res.body(), "field"));
  }

  @Test
  void shortPasswordReturns400OnPasswordField() throws Exception {
    HttpResponse<String> res = post("/auth/signup", credentials("short@test.com", "short"));

    assertEquals(400, res.statusCode());
    assertEquals("password", jsonString(res.body(), "field"));
  }

  @Test
  void wrongPasswordReturns401WithMessage() throws Exception {
    assertEquals(201,
        post("/auth/signup", credentials("wrongpw@test.com", "password123")).statusCode());

    HttpResponse<String> res = post("/auth/login",
        credentials("wrongpw@test.com", "not-the-password"));

    assertEquals(401, res.statusCode());
    assertEquals("Invalid email or password", jsonString(res.body(), "message"));
  }

  @Test
  void missingTokenReturns401() throws Exception {
    HttpResponse<String> res = send("GET", "/materials", null, null, null);

    assertEquals(401, res.statusCode());
    assertEquals("Authentication required", jsonString(res.body(), "message"));
    assertFalse(res.body().contains("\"field\""), res.body());
  }

  @Test
  void invalidTokenReturns401() throws Exception {
    HttpResponse<String> res = send("GET", "/materials", "not-a-jwt", null, null);

    assertEquals(401, res.statusCode());
    assertTrue(res.body().contains("\"status\":401"));
  }

  // ---------- helpers ----------

  private static String credentials(String email, String password) {
    return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
  }

  private HttpResponse<String> post(String path, String json) throws Exception {
    return send("POST", path, null, json, "application/json");
  }

  private HttpResponse<String> send(String method, String path, String token, String body,
      String contentType) throws Exception {
    HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    if (token != null) {
      b.header("Authorization", "Bearer " + token);
    }
    if (body != null) {
      b.header("Content-Type", contentType);
      b.method(method, HttpRequest.BodyPublishers.ofByteArray(body.getBytes(StandardCharsets.UTF_8)));
    } else {
      b.method(method, HttpRequest.BodyPublishers.noBody());
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  private static String jsonString(String body, String field) {
    Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(body);
    assertTrue(m.find(), field + " not found in: " + body);
    return m.group(1);
  }
}
