/**
 *
 */
package com.c4soft.bdp.labs;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.c4_soft.springaddons.security.oidc.starter.synchronised.resourceserver.JwtAbstractAuthenticationTokenConverter;
import com.c4soft.bdp.labs.user.Permission;
import com.c4soft.bdp.labs.user.Role;
import com.c4soft.bdp.labs.user.jpa.UserRolesService;
import lombok.RequiredArgsConstructor;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Component
  @RequiredArgsConstructor
  public class RestApiAuthenticationConverter implements JwtAbstractAuthenticationTokenConverter {

    private final UserRolesService userRolesService;

    @Override
    @Transactional(readOnly = false)
    public AbstractAuthenticationToken convert(Jwt jwt) {
      if (jwt == null) {
        return AnonymousAuthentication.instance;
      }

      final var sub = jwt.getSubject();

      final var roles = userRolesService.findBySub(sub);

      final var authorities = roles.stream().map(Role::getPermissions).flatMap(Set::stream)
          .map(Permission::getLabel).distinct().map(SimpleGrantedAuthority::new)
          .collect(Collectors.toSet());

      return new JwtAuthenticationToken(jwt, authorities);
    }

  }

}
