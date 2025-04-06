package com.ecommerce.api.controller;

import com.ecommerce.api.dto.AuthResponse;
import com.ecommerce.api.dto.UserLoginDto;
import com.ecommerce.api.dto.UserRegistrationDto;
import com.ecommerce.api.model.User;
import com.ecommerce.api.security.JwtTokenProvider;
import com.ecommerce.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
@SecurityRequirement(name = "bearer-auth")
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(summary = "Register a new user", description = "Creates a new user account with CUSTOMER role by default and returns JWT token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User successfully registered", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists")
  })
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
    User user = userService.registerUser(registrationDto);
    String token = jwtTokenProvider.generateToken(user.getUsername());
    return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
  }

  @Operation(summary = "Authenticate user", description = "Validates user credentials and returns JWT token for authentication")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginDto loginDto) {
    User user = userService.findByUsername(loginDto.getUsername());
    String token = jwtTokenProvider.generateToken(user.getUsername());
    return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
  }
}
