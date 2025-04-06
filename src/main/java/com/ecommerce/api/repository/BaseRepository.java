package com.ecommerce.ecomapi.repository;

import com.ecommerce.ecomapi.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity> extends JpaRepository<E, Long> {
  // Common repository methods can be added here
}
