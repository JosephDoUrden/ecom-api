package com.ecom.api.security.jwt;

import com.ecom.api.domain.model.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  @Value("${jwt.refresh-expiration}")
  private long refreshExpiration;

  private final TokenRepository tokenRepository;
  private final UserDetailsService userDetailsService;

  public JwtService(TokenRepository tokenRepository, UserDetailsService userDetailsService) {
    this.tokenRepository = tokenRepository;
    this.userDetailsService = userDetailsService;
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration);
  }

  private String buildToken(
      Map<String, Object> extraClaims,
      UserDetails userDetails,
      long expiration) {
    Date issuedAt = new Date(System.currentTimeMillis());
    Date expiryDate = new Date(System.currentTimeMillis() + expiration);

    String tokenValue = Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(issuedAt)
        .setExpiration(expiryDate)
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();

    // Store token info in Redis
    TokenInfo tokenInfo = TokenInfo.builder()
        .userId(extractUserId(userDetails))
        .username(userDetails.getUsername())
        .tokenValue(tokenValue)
        .roles(userDetails.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toSet()))
        .expiryDate(LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()))
        .revoked(false)
        .build();

    tokenRepository.save(tokenInfo);

    return tokenValue;
  }

  private String extractUserId(UserDetails userDetails) {
    // Extract user ID from UserDetails implementation
    // This depends on your UserDetails implementation
    return userDetails.getUsername(); // Placeholder - adjust as needed
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    // Check if token is valid in Redis
    TokenInfo tokenInfo = tokenRepository.findByTokenValue(token);
    boolean isValidInStorage = tokenInfo != null && !tokenInfo.isRevoked();

    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && isValidInStorage;
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts
        .parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public void revokeToken(String token) {
    TokenInfo tokenInfo = tokenRepository.findByTokenValue(token);
    if (tokenInfo != null) {
      tokenInfo.setRevoked(true);
      tokenRepository.save(tokenInfo);
    }
  }

  public void revokeAllUserTokens(String username) {
    Set<TokenInfo> userTokens = tokenRepository.findByUsername(username);
    if (userTokens != null && !userTokens.isEmpty()) {
      userTokens.forEach(token -> token.setRevoked(true));
      tokenRepository.saveAll(userTokens);
    }
  }

  /**
   * Validates a refresh token and generates new access and refresh tokens
   *
   * @param refreshToken the refresh token to validate
   * @return a map containing the new access and refresh tokens
   * @throws IllegalArgumentException if the refresh token is invalid or expired
   */
  public Map<String, String> refreshTokens(String refreshToken) {
    try {
      // Extract username from token
      final String username = extractUsername(refreshToken);

      if (username == null) {
        throw new IllegalArgumentException("Invalid refresh token");
      }

      // Find token in repository
      TokenInfo tokenInfo = tokenRepository.findByTokenValue(refreshToken);

      if (tokenInfo == null || tokenInfo.isRevoked()) {
        throw new IllegalArgumentException("Refresh token has been revoked");
      }

      // Check if token is for refresh purposes (based on expiration time)
      Date expiration = extractExpiration(refreshToken);
      if (expiration.getTime() - new Date().getTime() < jwtExpiration) {
        throw new IllegalArgumentException("Token is not a valid refresh token");
      }

      // Load user details
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      if (userDetails == null) {
        throw new IllegalArgumentException("User not found");
      }

      // Revoke the old refresh token
      revokeToken(refreshToken);

      // Generate new tokens
      String newAccessToken = generateToken(userDetails);
      String newRefreshToken = generateRefreshToken(userDetails);

      Map<String, String> tokens = new HashMap<>();
      tokens.put("accessToken", newAccessToken);
      tokens.put("refreshToken", newRefreshToken);

      return tokens;
    } catch (ExpiredJwtException e) {
      throw new IllegalArgumentException("Refresh token has expired");
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid refresh token: " + e.getMessage());
    }
  }
}
