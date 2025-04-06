package com.ecommerce.ecomapi.service.impl;

import com.ecommerce.ecomapi.dto.BaseDto;
import com.ecommerce.ecomapi.mapper.BaseMapper;
import com.ecommerce.ecomapi.model.BaseEntity;
import com.ecommerce.ecomapi.repository.BaseRepository;
import com.ecommerce.ecomapi.service.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseServiceImpl<E extends BaseEntity, D extends BaseDto> implements BaseService<E, D> {

  protected final BaseRepository<E> repository;
  protected final BaseMapper<E, D> mapper;

  protected BaseServiceImpl(BaseRepository<E> repository, BaseMapper<E, D> mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<D> findAll() {
    return repository.findAll().stream()
        .map(mapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<D> findById(Long id) {
    return repository.findById(id)
        .map(mapper::toDto);
  }

  @Override
  @Transactional
  public D save(D dto) {
    E entity = mapper.toEntity(dto);
    E savedEntity = repository.save(entity);
    return mapper.toDto(savedEntity);
  }

  @Override
  @Transactional
  public D update(Long id, D dto) {
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException("Entity with id " + id + " not found");
    }

    dto.setId(id);
    E entity = mapper.toEntity(dto);
    E updatedEntity = repository.save(entity);
    return mapper.toDto(updatedEntity);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException("Entity with id " + id + " not found");
    }
    repository.deleteById(id);
  }
}
