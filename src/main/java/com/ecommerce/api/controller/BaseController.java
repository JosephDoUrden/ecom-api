package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.BaseDto;
import com.ecommerce.ecomapi.model.BaseEntity;
import com.ecommerce.ecomapi.service.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseController<E extends BaseEntity, D extends BaseDto> {

  protected final BaseService<E, D> service;

  protected BaseController(BaseService<E, D> service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<D>> findAll() {
    return ResponseEntity.ok(service.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<D> findById(@PathVariable Long id) {
    return service.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<D> create(@RequestBody D dto) {
    return new ResponseEntity<>(service.save(dto), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<D> update(@PathVariable Long id, @RequestBody D dto) {
    try {
      return ResponseEntity.ok(service.update(id, dto));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    try {
      service.delete(id);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
