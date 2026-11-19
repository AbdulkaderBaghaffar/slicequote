package com.example.costestimator.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.costestimator.data.User;
import com.example.costestimator.dto.LoginRequest;
import com.example.costestimator.dto.LoginResponse;
import com.example.costestimator.dto.SignupRequest;
import com.example.costestimator.repository.UserRepository;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public LoginResponse signup(SignupRequest request) {
    String email = normalizeEmail(request.getEmail());

    if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
      throw new EmailAlreadyRegisteredException();
    }

    User user = new User();
    user.setEmail(email);
    user.setPasswordhash(passwordEncoder.encode(request.getPassword()));

    try {
      userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      // Unique-constraint race between the check above and the insert.
      throw new EmailAlreadyRegisteredException();
    }

    return new LoginResponse(issueToken(user));
  }

  public LoginResponse login(LoginRequest request) {
    String email = normalizeEmail(request.getEmail());

    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> invalidCredentials());

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw invalidCredentials();
    }

    return new LoginResponse(issueToken(user));
  }

  private String issueToken(User user) {
    return jwtService.generateToken(user.getEmail());
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private static ResponseStatusException invalidCredentials() {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
  }
}
