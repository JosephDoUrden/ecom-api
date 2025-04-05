package com.ecom.api.security.token;

import com.ecom.api.domain.model.TokenInfo;
import com.ecom.api.domain.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenManagementService {

  private final TokenRepository tokenRepository;

  /**
   * Stores a token in Redis
   */
  public void storeToken(TokenInfo tokenInfo, long expirationInSeconds) {
    String tokenId = UUID.randomUUID().toString();
    tokenRepository.saveToken(tokenId, tokenInfo, expirationInSeconds);
    log.debug("Token stored for user {}: {}", tokenInfo.getUsername(), tokenId);
  }

  /**
   * Validates if a token exists and is not revoked
   */
  public boolean validateToken(String tokenValue) {
    return tokenRepository.findByTokenValue(tokenValue)
        .map(token -> !token.isRevoked())
        .orElse(false);
  }

  /**
   * Revokes a specific token
   */
  public void revokeToken(String tokenValue) {
    tokenRepository.findByTokenValue(tokenValue).ifPresent(token -> {
      token.setRevoked(true);
      tokenRepository.saveToken(tokenValue, token,
          token.getExpiryDate().compareTo(LocalDateTime.now()) > 0
              ? token.getExpiryDate().getSecond() - LocalDateTime.now().getSecond()
              : 300); // Keep revoked tokens for a while
      log.debug("Token revoked for user {}", token.getUsername());
    });
  }

  /**
   * Revokes all tokens for a user
   */
  public void revokeAllUserTokens(String userId) {
    tokenRepository.revokeAllUserTokens(userId);
    log.debug("All tokens revoked for user ID {}", userId);
  }

  /**
   * Gets all active tokens for a user
   */
  public Set<TokenInfo> getUserActiveTokens(String userId) {
    return tokenRepository.findAllByUserId(userId);
  }

  /**
   * Scheduled job to clean up expired tokens
   */
  @Scheduled(fixedRate = 86400000) // Run once a day
  public void cleanupExpiredTokens() {
    log.info("Starting scheduled cleanup of expired tokens");
    tokenRepository.cleanupExpiredTokens();
    log.info("Completed cleanup of expired tokens");
  }
}
