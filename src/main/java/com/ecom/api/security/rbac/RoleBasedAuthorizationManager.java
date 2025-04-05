package com.ecom.api.security.rbac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Custom authorization manager for complex role-based access control scenarios.
 * This can be extended as needed for business-specific authorization
 * requirements.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleBasedAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

  private static final Set<String> ADMIN_ROLES = Set.of("admin");
  private static final Set<String> VENDOR_ROLES = Set.of("vendor", "admin");
  private static final Set<String> CUSTOMER_ROLES = Set.of("customer", "admin");

  @Override
  public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
    if (authentication == null) {
      return new AuthorizationDecision(false);
    }

    Authentication auth = authentication.get();
    if (auth == null || !auth.isAuthenticated()) {
      return new AuthorizationDecision(false);
    }

    String path = context.getRequest().getRequestURI();
    Object principal = auth.getPrincipal();

    if (principal instanceof Jwt jwt) {
      String userId = jwt.getSubject();
      log.debug("Checking authorization for user {} accessing {}", userId, path);

      // Handle custom authorization logic based on path patterns and roles
      if (path.startsWith("/api/admin/")) {
        return hasAnyRole(jwt, ADMIN_ROLES);
      } else if (path.startsWith("/api/vendor/")) {
        return hasAnyRole(jwt, VENDOR_ROLES);
      } else if (path.startsWith("/api/customer/")) {
        return hasAnyRole(jwt, CUSTOMER_ROLES);
      } else if (path.contains("/admin/")) {
        // For paths containing /admin/ segments in any API endpoint
        return hasAnyRole(jwt, ADMIN_ROLES);
      }

      // Additional business-specific authorization rules can be added here

      // Default authorization is granted if authenticated
      return new AuthorizationDecision(true);
    }

    return new AuthorizationDecision(false);
  }

  private AuthorizationDecision hasAnyRole(Jwt jwt, Set<String> roles) {
    @SuppressWarnings("unchecked")
    var realmAccess = (java.util.Map<String, Object>) jwt.getClaim("realm_access");
    if (realmAccess != null) {
      @SuppressWarnings("unchecked")
      var userRoles = (java.util.List<String>) realmAccess.get("roles");
      if (userRoles != null) {
        boolean hasRole = userRoles.stream()
            .anyMatch(roles::contains);
        return new AuthorizationDecision(hasRole);
      }
    }
    return new AuthorizationDecision(false);
  }
}
