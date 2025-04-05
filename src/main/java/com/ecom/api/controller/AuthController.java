package com.ecom.api.controller;

import com.ecom.api.dto.request.LoginRequest;
import com.ecom.api.dto.request.RegisterRequest;
import com.ecom.api.dto.response.LoginResponse;
import com.ecom.api.dto.response.RegisterResponse;
import com.ecom.api.security.jwt.JwtService;
import com.ecom.api.service.AuthService;
import com.ecom.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtService jwtService;
  private final UserService userService;
  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    try {
      RegisterResponse response = userService.registerUser(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      // Return error response
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    try {
      LoginResponse response = authService.login(request);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
    authService.logout(authHeader);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/logout-all-devices")
  public ResponseEntity<Void> logoutAllDevices(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      authService.logoutFromAllDevices(authentication.getName());
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> refreshRequest) {
    String refreshToken = refreshRequest.get("refreshToken");

    if (refreshToken == null || refreshToken.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Refresh token is required"));
    }

    try {
      // Validate the refresh token and generate new tokens
      Map<String, String> tokens = jwtService.refreshTokens(refreshToken);
      return ResponseEntity.ok(tokens);
    } catch (Exception e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
  }
}
