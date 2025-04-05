package com.ecom.api.controller;

import com.ecom.api.dto.request.UserProfileUpdateRequest;
import com.ecom.api.dto.response.UserProfileResponse;
import com.ecom.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserService userService;

  /**
   * Get the profile of the currently authenticated user
   */
  @GetMapping
  public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      UserProfileResponse profile = userService.getUserProfile(authentication.getName());
      return ResponseEntity.ok(profile);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Update the profile of the currently authenticated user
   */
  @PutMapping
  public ResponseEntity<?> updateCurrentUserProfile(
      Authentication authentication,
      @Valid @RequestBody UserProfileUpdateRequest request) {

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      UserProfileResponse updatedProfile = userService.updateUserProfile(authentication.getName(), request);
      return ResponseEntity.ok(updatedProfile);
    } catch (IllegalArgumentException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Deactivate the currently authenticated user's account
   */
  @PostMapping("/deactivate")
  public ResponseEntity<?> deactivateCurrentUserAccount(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    try {
      userService.deactivateUser(authentication.getName());
      Map<String, String> response = new HashMap<>();
      response.put("message", "Account successfully deactivated");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Admin endpoint to get any user's profile by username
   */
  @GetMapping("/admin/{username}")
  @PreAuthorize("hasRole('admin')")
  public ResponseEntity<?> getUserProfileByUsername(@PathVariable String username) {
    try {
      UserProfileResponse profile = userService.getUserProfile(username);
      return ResponseEntity.ok(profile);
    } catch (IllegalArgumentException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Admin endpoint to update any user's profile
   */
  @PutMapping("/admin/{username}")
  @PreAuthorize("hasRole('admin')")
  public ResponseEntity<?> updateUserProfileByAdmin(
      @PathVariable String username,
      @Valid @RequestBody UserProfileUpdateRequest request) {

    try {
      UserProfileResponse updatedProfile = userService.updateUserProfile(username, request);
      return ResponseEntity.ok(updatedProfile);
    } catch (IllegalArgumentException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Admin endpoint to activate/deactivate a user account
   */
  @PostMapping("/admin/{username}/{action}")
  @PreAuthorize("hasRole('admin')")
  public ResponseEntity<?> setUserAccountStatus(
      @PathVariable String username,
      @PathVariable String action) {

    try {
      boolean success;
      String message;

      if ("activate".equals(action)) {
        success = userService.reactivateUser(username);
        message = "Account successfully activated";
      } else if ("deactivate".equals(action)) {
        success = userService.deactivateUser(username);
        message = "Account successfully deactivated";
      } else {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid action. Use 'activate' or 'deactivate'"));
      }

      Map<String, String> response = new HashMap<>();
      response.put("message", message);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    } catch (Exception e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }
}
