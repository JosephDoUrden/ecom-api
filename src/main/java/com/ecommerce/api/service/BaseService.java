package com.ecommerce.api.service;

import java.util.List;
import java.util.Optional;

import com.ecommerce.api.dto.BaseDto;
import com.ecommerce.api.model.BaseEntity;

public interface BaseService<E extends BaseEntity, D extends BaseDto> {

  List<D> findAll();

  Optional<D> findById(Long id);

  D save(D dto);

  D update(Long id, D dto);

  void delete(Long id);
}
