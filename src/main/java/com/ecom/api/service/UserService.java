package com.ecom.api.service;

import com.ecom.api.domain.entity.User;
import com.ecom.api.domain.repository.UserRepository;
import com.ecom.api.dto.request.RegisterRequest;
import com.ecom.api.dto.request.UserProfileUpdateRequest;
import com.ecom.api.dto.response.RegisterResponse;
import com.ecom.api.dto.response.UserProfileResponse;
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

  /**
   * Get a user profile by username
   *
   * @param username the username of the user
   * @return the user profile
   * @throws IllegalArgumentException if the user is not found
   */
  public UserProfileResponse getUserProfile(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    return mapToUserProfileResponse(user);
  }

  /**
   * Update a user profile
   *
   * @param username the username of the user
   * @param request  the update request
   * @return the updated user profile
   * @throws IllegalArgumentException if the user is not found or email is already
   *                                  taken
   */
  @Transactional
  public UserProfileResponse updateUserProfile(String username, UserProfileUpdateRequest request) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // If email is changing, check if it's already taken
    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
      if (userRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Email is already in use");
      }
      user.setEmail(request.getEmail());
    }

    // Update fields if provided
    if (request.getFirstName() != null) {
      user.setFirstName(request.getFirstName());
    }

    if (request.getLastName() != null) {
      user.setLastName(request.getLastName());
    }

    if (request.getPhoneNumber() != null) {
      user.setPhoneNumber(request.getPhoneNumber());
    }

    if (request.getAddress() != null) {
      user.setAddress(request.getAddress());
    }

    if (request.getCity() != null) {
      user.setCity(request.getCity());
    }

    if (request.getState() != null) {
      user.setState(request.getState());
    }

    if (request.getZipCode() != null) {
      user.setZipCode(request.getZipCode());
    }

    if (request.getCountry() != null) {
      user.setCountry(request.getCountry());
    }

    // Save updated user
    User updatedUser = userRepository.save(user);

    return mapToUserProfileResponse(updatedUser);
  }

  /**
   * Deactivate a user account
   *
   * @param username the username of the user
   * @return true if successful
   * @throws IllegalArgumentException if the user is not found
   */
  @Transactional
  public boolean deactivateUser(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.setActive(false);
    userRepository.save(user);

    // Revoke all user tokens
    jwtService.revokeAllUserTokens(username);

    return true;
  }

  /**
   * Reactivate a user account
   *
   * @param username the username of the user
   * @return true if successful
   * @throws IllegalArgumentException if the user is not found
   */
  @Transactional
  public boolean reactivateUser(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.setActive(true);
    userRepository.save(user);

    return true;
  }

  /**
   * Map User entity to UserProfileResponse
   *
   * @param user the user entity
   * @return the user profile response
   */
  private UserProfileResponse mapToUserProfileResponse(User user) {
    return UserProfileResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .phoneNumber(user.getPhoneNumber())
        .address(user.getAddress())
        .city(user.getCity())
        .state(user.getState())
        .zipCode(user.getZipCode())
        .country(user.getCountry())
        .active(user.isActive())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
