package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.dto.BaseDto;
import com.ecommerce.ecomapi.model.BaseEntity;

import java.util.List;
import java.util.Optional;

public interface BaseService<E extends BaseEntity, D extends BaseDto> {

  List<D> findAll();

  Optional<D> findById(Long id);

  D save(D dto);

  D update(Long id, D dto);

  void delete(Long id);
}
