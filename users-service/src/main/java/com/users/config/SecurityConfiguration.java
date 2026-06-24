package com.users.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

/** Security Configuration Class. */
@Log4j2
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

  @Bean
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.authorizeHttpRequests(
            authz ->
                authz.requestMatchers("/users/**").authenticated().anyRequest().authenticated())
        .csrf(AbstractHttpConfigurer::disable);
    http.oauth2ResourceServer(
        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRolesConverter());
    return converter;
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web ->
        web.ignoring()
            .requestMatchers(
                "/error", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html");
  }

  @Bean
  JwtDecoder jwtDecoder(OAuth2ResourceServerProperties properties) {
    String issuerUri = properties.getJwt().getIssuerUri();
    return JwtDecoders.fromOidcIssuerLocation(issuerUri);
  }

  /**
   * Custom converter that maps Keycloak roles from both realm_access.roles and
   * resource_access.<client>.roles to Spring Security ROLE_* authorities.
   */
  static class KeycloakRealmRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter defaultConverter =
        new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
      Set<GrantedAuthority> authorities = new HashSet<>();

      // Map realm_access.roles (default Spring behavior)
      authorities.addAll(defaultConverter.convert(jwt));

      // Map resource_access.<client>.roles
      Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
      if (resourceAccess != null) {
        for (var entry : resourceAccess.entrySet()) {
          Object value = entry.getValue();
          if (value instanceof Map<?, ?> clientEntry) {
            Object roles = clientEntry.get("roles");
            if (roles instanceof List<?> roleList) {
              for (Object role : roleList) {
                if (role instanceof String roleName) {
                  authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                }
              }
            }
          }
        }
      }

      return new ArrayList<>(authorities);
    }
  }
}
