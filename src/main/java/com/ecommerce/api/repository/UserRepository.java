package com.ecommerce.api.repository;

import com.ecommerce.api.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);
}
