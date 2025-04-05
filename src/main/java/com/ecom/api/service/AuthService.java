package com.ecom.api.service;

import com.ecom.api.domain.entity.User;
import com.ecom.api.domain.repository.UserRepository;
import com.ecom.api.dto.request.LoginRequest;
import com.ecom.api.dto.response.LoginResponse;
import com.ecom.api.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  /**
   * Authenticate a user and generate access tokens
   *
   * @param request the login request containing credentials
   * @return login response with user details and tokens
   */
  public LoginResponse login(LoginRequest request) {
    try {
      // Determine if the input is email or username
      String username = request.getUsernameOrEmail();
      if (request.getUsernameOrEmail().contains("@")) {
        Optional<User> userOptional = userRepository.findByEmail(request.getUsernameOrEmail());
        if (userOptional.isPresent()) {
          username = userOptional.get().getUsername();
        }
      }

      // Authenticate using Spring Security
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, request.getPassword()));

      // Get user details
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      // Find the corresponding user entity
      User user = userRepository.findByUsername(userDetails.getUsername())
          .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

      // Generate tokens
      String accessToken = jwtService.generateToken(userDetails);
      String refreshToken = jwtService.generateRefreshToken(userDetails);

      Map<String, String> tokens = new HashMap<>();
      tokens.put("accessToken", accessToken);
      tokens.put("refreshToken", refreshToken);

      return LoginResponse.builder()
          .userId(user.getId())
          .username(user.getUsername())
          .email(user.getEmail())
          .tokens(tokens)
          .build();
    } catch (AuthenticationException e) {
      throw new IllegalArgumentException("Invalid username/email or password");
    }
  }

  /**
   * Logout a user by invalidating their token
   *
   * @param token the access token to invalidate
   */
  public void logout(String token) {
    if (token != null && !token.isEmpty()) {
      // Remove "Bearer " if present
      if (token.startsWith("Bearer ")) {
        token = token.substring(7);
      }
      jwtService.revokeToken(token);
    }
  }

  /**
   * Logout user from all devices by invalidating all their tokens
   *
   * @param username the username of the user
   */
  public void logoutFromAllDevices(String username) {
    jwtService.revokeAllUserTokens(username);
  }
}
