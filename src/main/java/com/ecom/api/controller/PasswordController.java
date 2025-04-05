package com.ecom.api.controller;

import com.ecom.api.dto.request.PasswordChangeRequest;
import com.ecom.api.dto.request.PasswordResetConfirmRequest;
import com.ecom.api.dto.request.PasswordResetRequest;
import com.ecom.api.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {

  private final PasswordService passwordService;

  /**
   * Request a password reset by providing an email address
   */
  @PostMapping("/reset-request")
  public ResponseEntity<Map<String, String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
    // Always return success even if email not found for security
    passwordService.requestPasswordReset(request);

    Map<String, String> response = new HashMap<>();
    response.put("message", "If your email is registered, you will receive a password reset link shortly");
    return ResponseEntity.ok(response);
  }

  /**
   * Confirm password reset with token and new password
   */
  @PostMapping("/reset-confirm")
  public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
    try {
      passwordService.confirmPasswordReset(request);

      Map<String, String> response = new HashMap<>();
      response.put("message", "Password has been reset successfully");
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }

  /**
   * Change password (requires authentication)
   */
  @PostMapping("/change")
  public ResponseEntity<?> changePassword(
      Authentication authentication,
      @Valid @RequestBody PasswordChangeRequest request) {

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      passwordService.changePassword(authentication.getName(), request);

      Map<String, String> response = new HashMap<>();
      response.put("message", "Password changed successfully");
      return ResponseEntity.ok(response);
    } catch (BadCredentialsException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Current password is incorrect");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }
}
