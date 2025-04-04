package com.ecom.api.security.jwt;

import com.ecom.api.domain.model.TokenInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TokenRepository extends CrudRepository<TokenInfo, String> {
  TokenInfo findByTokenValue(String tokenValue);

  Set<TokenInfo> findByUsername(String username);

  Set<TokenInfo> findByUserId(String userId);

  void deleteByUsername(String username);
}
