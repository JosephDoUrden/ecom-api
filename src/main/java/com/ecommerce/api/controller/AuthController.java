package com.ecommerce.api.controller;

import com.ecommerce.api.dto.AuthResponse;
import com.ecommerce.api.dto.UserLoginDto;
import com.ecommerce.api.dto.UserRegistrationDto;
import com.ecommerce.api.model.User;
import com.ecommerce.api.security.JwtTokenProvider;
import com.ecommerce.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
    User user = userService.registerUser(registrationDto);
    String token = jwtTokenProvider.generateToken(user.getUsername());
    return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginDto loginDto) {
    User user = userService.findByUsername(loginDto.getUsername());
    String token = jwtTokenProvider.generateToken(user.getUsername());
    return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
  }
}
