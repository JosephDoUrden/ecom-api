package com.ecom.api.controller;

import com.ecom.api.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtService jwtService;

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
