package com.ecom.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(requests -> requests
            .requestMatchers("/api/public/**", "/actuator/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("admin")
            .requestMatchers("/api/vendor/**").hasRole("vendor")
            .requestMatchers("/api/customer/**").hasRole("customer")
            .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
            .requestMatchers("/api/password/reset-request", "/api/password/reset-confirm").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
    return jwtConverter;
  }

  @Bean
  public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
    JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();

    return jwt -> {
      Collection<GrantedAuthority> authorities = delegate.convert(jwt);

      if (authorities == null) {
        authorities = List.of();
      }

      // Extract realm roles from the JWT claims
      Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
      if (realmAccess != null) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles != null) {
          // Add ROLE_ prefix to realm roles for Spring Security compatibility
          Collection<GrantedAuthority> realmRoles = roles.stream()
              .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
              .collect(Collectors.toList());

          // Merge realm roles with existing authorities
          return Stream.concat(authorities.stream(), realmRoles.stream())
              .collect(Collectors.toList());
        }
      }

      return authorities;
    };
  }
}
