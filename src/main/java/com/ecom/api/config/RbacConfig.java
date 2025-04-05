package com.ecom.api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration for RBAC (Role-Based Access Control)
 * This setup enhances Spring Security's method-level security with additional
 * logging
 * and custom authorization managers if needed.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class RbacConfig {

  /**
   * Create a custom advisor for @PreAuthorize annotations to add logging
   */
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public Advisor preAuthorizeAuthorizationMethodInterceptor() {
    AnnotationMatchingPointcut pointcut = new AnnotationMatchingPointcut(null, PreAuthorize.class);
    // Create the interceptor using the pointcut directly in the constructor or
    // factory method
    AuthorizationManagerBeforeMethodInterceptor interceptor = AuthorizationManagerBeforeMethodInterceptor
        .preAuthorize();

    log.info("Initialized RBAC Pre-authorization interceptor");
    return interceptor;
  }
}
