package com.ecommerce.api.service;

import com.ecommerce.api.dto.UserRegistrationDto;
import com.ecommerce.api.model.User;

public interface UserService {

  User registerUser(UserRegistrationDto userRegistrationDto);

  User findByUsername(String username);
}
