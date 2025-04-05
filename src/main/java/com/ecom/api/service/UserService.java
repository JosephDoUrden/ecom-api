package com.ecom.api.service;

import com.ecom.api.domain.entity.User;
import com.ecom.api.domain.repository.UserRepository;
import com.ecom.api.dto.request.RegisterRequest;
import com.ecom.api.dto.response.RegisterResponse;
import com.ecom.api.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  /**
   * Register a new user
   * 
   * @param request the registration request
   * @return the registration response with user info and tokens
   */
  @Transactional
  public RegisterResponse registerUser(RegisterRequest request) {
    // Check if username or email already exists
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    // Create new user entity
    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .active(true)
        .build();

    // Save user to database
    User savedUser = userRepository.save(user);

    // Generate tokens
    UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
    String accessToken = jwtService.generateToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    Map<String, String> tokens = new HashMap<>();
    tokens.put("accessToken", accessToken);
    tokens.put("refreshToken", refreshToken);

    // Build and return response
    return RegisterResponse.builder()
        .userId(savedUser.getId())
        .username(savedUser.getUsername())
        .email(savedUser.getEmail())
        .active(savedUser.isActive())
        .tokens(tokens)
        .build();
  }
}
