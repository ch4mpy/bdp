package com.c4soft.bdp.labs.me;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Gateway")
@RestController
@RequestMapping(
    produces = {MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Observed
@Slf4j
public class MeController {
  public static final String BASE_PATH = "/me";

  /**
   * Returns information of the current user if authenticated, ANONYMOUS otherwise
   * 
   * @param auth
   * @return
   */
  @GetMapping(path = BASE_PATH)
  public UserResponse getMe(Authentication auth) {
    if (auth instanceof OAuth2AuthenticationToken oauth
        && oauth.getPrincipal() instanceof OidcUser oidcUser) {
      return new UserResponse(
          oauth.getName(),
          oidcUser.getAttributes().getOrDefault(StandardClaimNames.EMAIL, "").toString(),
          oidcUser
              .getAttributes()
              .getOrDefault(StandardClaimNames.PREFERRED_USERNAME, "")
              .toString(),
          oidcUser.getAttributes().getOrDefault(StandardClaimNames.GIVEN_NAME, "").toString(),
          oidcUser.getAttributes().getOrDefault(StandardClaimNames.FAMILY_NAME, "").toString(),
          oauth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
    return UserResponse.ANONYMOUS;
  }

}
