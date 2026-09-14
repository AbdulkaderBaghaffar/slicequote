package com.example.costestimator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.example.costestimator.service.*;
import com.example.costestimator.dto.SignupRequest;

@RestController
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/auth/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public void signup(@RequestBody SignupRequest request) {
    userService.signup(request);
  }
}
