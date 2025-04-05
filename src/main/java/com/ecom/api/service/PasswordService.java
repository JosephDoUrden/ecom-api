package com.ecom.api.service;

import com.ecom.api.domain.entity.User;
import com.ecom.api.domain.repository.UserRepository;
import com.ecom.api.dto.request.PasswordChangeRequest;
import com.ecom.api.dto.request.PasswordResetConfirmRequest;
import com.ecom.api.dto.request.PasswordResetRequest;
import com.ecom.api.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PasswordService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RedisTemplate<String, String> redisTemplate;
  private final EmailService emailService; // Assume this service is implemented

  private static final String RESET_TOKEN_PREFIX = "password_reset:";
  private static final long RESET_TOKEN_EXPIRY_MINUTES = 30;

  /**
   * Initiates the password reset process by generating a reset token
   * and sending it to the user's email address
   *
   * @param request the password reset request containing the user's email
   * @return true if the password reset email was sent, false otherwise
   */
  public boolean requestPasswordReset(PasswordResetRequest request) {
    Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

    if (userOptional.isEmpty()) {
      // Don't reveal that the email doesn't exist for security reasons
      return false;
    }

    User user = userOptional.get();

    // Generate a secure random token
    String resetToken = generateSecureToken();

    // Store token in Redis with expiration
    String redisKey = RESET_TOKEN_PREFIX + resetToken;
    redisTemplate.opsForValue().set(redisKey, user.getUsername(), Duration.ofMinutes(RESET_TOKEN_EXPIRY_MINUTES));

    // Send email with reset link
    String resetLink = "https://your-frontend-app.com/reset-password?token=" + resetToken;
    emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);

    return true;
  }

  /**
   * Confirms a password reset request using a valid token and sets the new
   * password
   *
   * @param request the password reset confirmation request
   * @return true if the password was reset successfully
   * @throws IllegalArgumentException if the token is invalid or expired
   */
  @Transactional
  public boolean confirmPasswordReset(PasswordResetConfirmRequest request) {
    String redisKey = RESET_TOKEN_PREFIX + request.getToken();
    String username = redisTemplate.opsForValue().get(redisKey);

    if (username == null) {
      throw new IllegalArgumentException("Invalid or expired password reset token");
    }

    // Find user by username
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalStateException("User not found"));

    // Update password
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Delete the token so it can't be used again
    redisTemplate.delete(redisKey);

    // Invalidate all existing tokens for this user for security
    jwtService.revokeAllUserTokens(username);

    return true;
  }

  /**
   * Changes a user's password after verifying their current password
   *
   * @param username the username of the user
   * @param request  the password change request
   * @return true if password was changed successfully
   * @throws BadCredentialsException if the current password is incorrect
   */
  @Transactional
  public boolean changePassword(String username, PasswordChangeRequest request) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Verify current password
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new BadCredentialsException("Current password is incorrect");
    }

    // Update password
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Invalidate all existing tokens except current one for security
    jwtService.revokeAllUserTokens(username);

    return true;
  }

  /**
   * Generates a secure random token for password reset
   *
   * @return a secure random token
   */
  private String generateSecureToken() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] tokenBytes = new byte[32]; // 256 bits
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }
}
