package com.ecom.api.domain.repository;

import com.ecom.api.domain.model.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TokenRepository {

  private static final String TOKEN_KEY_PREFIX = "token:";
  private static final String USER_TOKENS_PREFIX = "user:tokens:";
  private static final String ALL_TOKENS_KEY = "all:tokens";

  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * Saves a token to Redis with the specified expiration time
   */
  public void saveToken(String tokenId, TokenInfo tokenInfo, long expirationInSeconds) {
    String tokenKey = TOKEN_KEY_PREFIX + tokenId;
    String userTokensKey = USER_TOKENS_PREFIX + tokenInfo.getUserId();

    redisTemplate.opsForValue().set(tokenKey, tokenInfo, expirationInSeconds, TimeUnit.SECONDS);
    redisTemplate.opsForSet().add(userTokensKey, tokenId);
    redisTemplate.opsForSet().add(ALL_TOKENS_KEY, tokenId);

    // Set expiration for user tokens collection (30 days longer than the token
    // itself)
    redisTemplate.expire(userTokensKey, expirationInSeconds + (30 * 24 * 60 * 60), TimeUnit.SECONDS);
  }

  /**
   * Finds a token by its ID
   */
  public Optional<TokenInfo> findById(String tokenId) {
    String tokenKey = TOKEN_KEY_PREFIX + tokenId;
    TokenInfo tokenInfo = (TokenInfo) redisTemplate.opsForValue().get(tokenKey);
    return Optional.ofNullable(tokenInfo);
  }

  /**
   * Finds a token by its value
   */
  public Optional<TokenInfo> findByTokenValue(String tokenValue) {
    // Since we're using the token value as the ID in Redis, we can just look it up
    return findById(tokenValue);
  }

  /**
   * Deletes a token from Redis
   */
  public void deleteToken(String tokenId, String userId) {
    String tokenKey = TOKEN_KEY_PREFIX + tokenId;
    String userTokensKey = USER_TOKENS_PREFIX + userId;

    redisTemplate.delete(tokenKey);
    redisTemplate.opsForSet().remove(userTokensKey, tokenId);
    redisTemplate.opsForSet().remove(ALL_TOKENS_KEY, tokenId);
  }

  /**
   * Revokes all tokens for a specific user
   */
  public void revokeAllUserTokens(String userId) {
    String userTokensKey = USER_TOKENS_PREFIX + userId;

    Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokensKey);
    if (tokenIds != null) {
      tokenIds.forEach(tokenId -> {
        String tokenKey = TOKEN_KEY_PREFIX + tokenId;
        TokenInfo tokenInfo = (TokenInfo) redisTemplate.opsForValue().get(tokenKey);
        if (tokenInfo != null) {
          tokenInfo.setRevoked(true);
          redisTemplate.opsForValue().set(tokenKey, tokenInfo);
        }
      });
    }
  }

  /**
   * Finds all tokens for a specific user
   */
  public Set<TokenInfo> findAllByUserId(String userId) {
    String userTokensKey = USER_TOKENS_PREFIX + userId;
    Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokensKey);

    if (tokenIds == null) {
      return new HashSet<>();
    }

    return tokenIds.stream()
        .map(id -> TOKEN_KEY_PREFIX + id)
        .map(key -> (TokenInfo) redisTemplate.opsForValue().get(key))
        .filter(token -> token != null)
        .collect(Collectors.toSet());
  }

  /**
   * Finds all tokens for a specific username
   */
  public Set<TokenInfo> findAllByUsername(String username) {
    Set<Object> allTokenIds = redisTemplate.opsForSet().members(ALL_TOKENS_KEY);

    if (allTokenIds == null) {
      return new HashSet<>();
    }

    return allTokenIds.stream()
        .map(id -> TOKEN_KEY_PREFIX + id)
        .map(key -> (TokenInfo) redisTemplate.opsForValue().get(key))
        .filter(token -> token != null && username.equals(token.getUsername()))
        .collect(Collectors.toSet());
  }

  /**
   * Removes expired tokens for housekeeping
   */
  public void cleanupExpiredTokens() {
    // Redis automatically removes expired keys, so we just need to clean up
    // references
    Set<Object> allTokenIds = redisTemplate.opsForSet().members(ALL_TOKENS_KEY);

    if (allTokenIds != null) {
      allTokenIds.forEach(id -> {
        String tokenKey = TOKEN_KEY_PREFIX + id;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(tokenKey))) {
          // Token has expired or been removed, clean up the references
          redisTemplate.opsForSet().remove(ALL_TOKENS_KEY, id);

          // Find and clean up user references
          String username = id.toString().split(":")[0]; // Extract username from token id if encoded that way
          redisTemplate.keys(USER_TOKENS_PREFIX + "*")
              .forEach(userKey -> redisTemplate.opsForSet().remove(userKey, id));
        }
      });
    }
  }
}
