package com.ecom.api.controller;

import com.ecom.api.domain.model.TokenInfo;
import com.ecom.api.security.token.TokenManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenManagementController {

  private final TokenManagementService tokenManagementService;

  /**
   * Lists active sessions for the current user
   */
  @GetMapping("/active-sessions")
  public ResponseEntity<Set<TokenInfo>> getActiveSessions(@AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getSubject();
    Set<TokenInfo> activeTokens = tokenManagementService.getUserActiveTokens(userId);

    // Remove token values for security
    activeTokens.forEach(token -> token.setTokenValue("[PROTECTED]"));

    return ResponseEntity.ok(activeTokens);
  }

  /**
   * Revokes a specific session
   */
  @DeleteMapping("/{tokenId}")
  public ResponseEntity<Void> revokeSession(@PathVariable String tokenId, @AuthenticationPrincipal Jwt jwt) {
    tokenManagementService.revokeToken(tokenId);
    return ResponseEntity.ok().build();
  }

  /**
   * Revokes all sessions for the current user except the current one
   */
  @DeleteMapping("/revoke-all-except-current")
  public ResponseEntity<Void> revokeAllExceptCurrent(
      @AuthenticationPrincipal Jwt jwt,
      @RequestHeader("Authorization") String authHeader) {

    String currentToken = authHeader.substring(7); // Remove "Bearer " prefix
    Set<TokenInfo> activeTokens = tokenManagementService.getUserActiveTokens(jwt.getSubject());

    activeTokens.stream()
        .filter(token -> !token.getTokenValue().equals(currentToken))
        .forEach(token -> tokenManagementService.revokeToken(token.getTokenValue()));

    return ResponseEntity.ok().build();
  }

  /**
   * Admin endpoint to revoke all tokens for a specific user
   */
  @DeleteMapping("/admin/revoke-user/{userId}")
  @PreAuthorize("hasRole('admin')")
  public ResponseEntity<Void> revokeAllUserTokens(@PathVariable String userId) {
    tokenManagementService.revokeAllUserTokens(userId);
    return ResponseEntity.ok().build();
  }
}
