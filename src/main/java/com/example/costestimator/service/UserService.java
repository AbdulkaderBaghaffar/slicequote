package com.example.costestimator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

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

  public User signup(SignupRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPasswordhash(passwordEncoder.encode(request.getPassword()));
    return userRepository.save((user));
  }

  public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

    if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      return new LoginResponse(jwtService.generateToken(user.getEmail()));
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");

  }
}
