package com.ecom.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo implements Serializable {
  private String userId;
  private String username;
  private String tokenValue;
  private Set<String> roles;
  private LocalDateTime expiryDate;
  private boolean revoked;
}
