package com.example.costestimator.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.costestimator.data.User;
import com.example.costestimator.dto.SignupRequest;
import com.example.costestimator.repository.UserRepository;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User signup(SignupRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPasswordhash(passwordEncoder.encode(request.getPassword()));
    return userRepository.save((user));
  }
}
