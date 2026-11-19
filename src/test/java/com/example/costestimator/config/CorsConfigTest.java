package com.example.costestimator.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CorsConfigTest {

  @Value("${local.server.port}")
  int port;

  private final HttpClient http = HttpClient.newHttpClient();

  @Test
  void preflightFromAllowedOriginReturns200() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + port + "/materials"))
        .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
        .header("Origin", "http://localhost:5173")
        .header("Access-Control-Request-Method", "GET")
        .header("Access-Control-Request-Headers", "authorization")
        .build();

    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode());
    assertEquals("http://localhost:5173",
        response.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
    assertTrue(response.headers().firstValue("Access-Control-Allow-Headers").orElse("")
        .toLowerCase().contains("authorization"));
  }

  @Test
  void preflightFromUnknownOriginIsRejected() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:" + port + "/materials"))
        .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
        .header("Origin", "http://evil.example")
        .header("Access-Control-Request-Method", "GET")
        .build();

    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(403, response.statusCode());
    assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
  }
}
