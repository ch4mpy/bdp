/**
 *
 */
package com.c4soft.bdp.labs.user.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@RestController
@Observed
public class MeController {

  @GetMapping("/me")
  @Transactional(readOnly = true)
  @Operation(description = "Information of the current user if authenticated, ANONYMOUS otherwise")
  public MeResponse getMe(Authentication auth) {
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      return new MeResponse(jwtAuth.getName(),
          jwtAuth.getTokenAttributes().getOrDefault(StandardClaimNames.EMAIL, "").toString(),
          jwtAuth.getTokenAttributes().getOrDefault(StandardClaimNames.PREFERRED_USERNAME, "").toString(),
          jwtAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
    return MeResponse.ANONYMOUS;
  }

}
