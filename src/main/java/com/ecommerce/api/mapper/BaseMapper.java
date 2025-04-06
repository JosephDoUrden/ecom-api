package com.ecommerce.api.mapper;

import com.ecommerce.api.dto.BaseDto;
import com.ecommerce.api.model.BaseEntity;

public interface BaseMapper<E extends BaseEntity, D extends BaseDto> {

  E toEntity(D dto);

  D toDto(E entity);
}
