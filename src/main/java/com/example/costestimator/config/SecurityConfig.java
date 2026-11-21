package com.example.costestimator.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.costestimator.dto.ApiError;
import com.example.costestimator.security.JwtAuthFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder PasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthFilter jwtAuthFilter,
      ObjectMapper objectMapper) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) ->
                writeError(objectMapper, response, 401, "Unauthorized", "Authentication required"))
            .accessDeniedHandler((request, response, accessDeniedException) ->
                writeError(objectMapper, response, 403, "Forbidden", "Access denied")))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/auth/**", "/error", "/actuator/health").permitAll()
            .requestMatchers(HttpMethod.GET, "/materials").permitAll()
            .requestMatchers(HttpMethod.POST, "/uploads").permitAll()
            .requestMatchers(HttpMethod.GET, "/uploads/*").permitAll()
            .requestMatchers(HttpMethod.POST, "/uploads/*/reprice").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  private static void writeError(
      ObjectMapper objectMapper,
      HttpServletResponse response,
      int status,
      String error,
      String message) throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(response.getWriter(), new ApiError(status, error, message));
  }
}
