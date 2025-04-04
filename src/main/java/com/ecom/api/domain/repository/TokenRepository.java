package com.ecom.api.domain.repository;

import com.ecom.api.domain.model.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenRepository {

  private static final String TOKEN_KEY_PREFIX = "token:";
  private static final String USER_TOKENS_PREFIX = "user:tokens:";

  private final RedisTemplate<String, Object> redisTemplate;

  public void saveToken(String tokenId, TokenInfo tokenInfo, long expirationInSeconds) {
    String tokenKey = TOKEN_KEY_PREFIX + tokenId;
    String userTokensKey = USER_TOKENS_PREFIX + tokenInfo.getUserId();

    redisTemplate.opsForValue().set(tokenKey, tokenInfo, expirationInSeconds, TimeUnit.SECONDS);
    redisTemplate.opsForSet().add(userTokensKey, tokenId);
    redisTemplate.expire(userTokensKey, 30, TimeUnit.DAYS);
  }

  public Optional<TokenInfo> findById(String tokenId) {
    String tokenKey = TOKEN_KEY_PREFIX + tokenId;
    TokenInfo tokenInfo = (TokenInfo) redisTemplate.opsForValue().get(tokenKey);
    return Optional.ofNullable(tokenInfo);
  }

  public void deleteToken(String tokenId, String userId) {
    String tokenKey = TOKEN_KEY_PREFIX + tokenId;
    String userTokensKey = USER_TOKENS_PREFIX + userId;

    redisTemplate.delete(tokenKey);
    redisTemplate.opsForSet().remove(userTokensKey, tokenId);
  }

  public void revokeAllUserTokens(String userId) {
    String userTokensKey = USER_TOKENS_PREFIX + userId;

    redisTemplate.opsForSet().members(userTokensKey).forEach(tokenId -> {
      String tokenKey = TOKEN_KEY_PREFIX + tokenId;
      TokenInfo tokenInfo = (TokenInfo) redisTemplate.opsForValue().get(tokenKey);
      if (tokenInfo != null) {
        tokenInfo.setRevoked(true);
        redisTemplate.opsForValue().set(tokenKey, tokenInfo);
      }
    });
  }
}
