package com.ecom.api.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SecuredController {

  @GetMapping("/customer/profile")
  @PreAuthorize("hasRole('customer')")
  public Map<String, Object> getCustomerProfile(@AuthenticationPrincipal Jwt jwt) {
    return createUserInfo(jwt, "customer");
  }

  @GetMapping("/vendor/dashboard")
  @PreAuthorize("hasRole('vendor')")
  public Map<String, Object> getVendorDashboard(@AuthenticationPrincipal Jwt jwt) {
    return createUserInfo(jwt, "vendor");
  }

  @GetMapping("/admin/dashboard")
  @PreAuthorize("hasRole('admin')")
  public Map<String, Object> getAdminDashboard(@AuthenticationPrincipal Jwt jwt) {
    return createUserInfo(jwt, "admin");
  }

  private Map<String, Object> createUserInfo(Jwt jwt, String role) {
    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("sub", jwt.getSubject());
    userInfo.put("name", jwt.getClaim("name"));
    userInfo.put("email", jwt.getClaim("email"));
    userInfo.put("role", role);
    userInfo.put("timestamp", System.currentTimeMillis());
    return userInfo;
  }
}
