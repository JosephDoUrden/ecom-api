package com.ecommerce.ecomapi.mapper;

import com.ecommerce.ecomapi.dto.BaseDto;
import com.ecommerce.ecomapi.model.BaseEntity;

public interface BaseMapper<E extends BaseEntity, D extends BaseDto> {

  E toEntity(D dto);

  D toDto(E entity);
}
