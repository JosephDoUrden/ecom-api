package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.UserRegistrationDto;
import com.ecommerce.api.model.User;
import com.ecommerce.api.model.UserRole;
import com.ecommerce.api.repository.UserRepository;
import com.ecommerce.api.service.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public User registerUser(UserRegistrationDto userRegistrationDto) {
    if (userRepository.findByUsername(userRegistrationDto.getUsername()).isPresent()) {
      throw new EntityExistsException("Username already exists");
    }
    if (userRepository.findByEmail(userRegistrationDto.getEmail()).isPresent()) {
      throw new EntityExistsException("Email already exists");
    }

    User user = new User();
    user.setUsername(userRegistrationDto.getUsername());
    user.setPassword(passwordEncoder.encode(userRegistrationDto.getPassword()));
    user.setEmail(userRegistrationDto.getEmail());
    user.setRoles(Set.of(UserRole.CUSTOMER)); // Default role

    return userRepository.save(user);
  }

  @Override
  public User findByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
  }
}
