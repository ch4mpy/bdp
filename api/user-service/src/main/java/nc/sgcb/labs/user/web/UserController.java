package nc.sgcb.labs.user.web;

import java.net.URI;
import java.util.Optional;
import org.keycloak.admin.api.UsersApi;
import org.keycloak.admin.model.UserRepresentation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Tag(name = "Users")
@RestController
@RequestMapping(
    produces = {MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Observed
@Slf4j
public class UserController {
  public static final String BASE_PATH = "/users";
  public static final String ME_PATH = BASE_PATH + "/me";
  public static final String USER_PLACEHOLDER = "userSub";
  public static final String USER_PATH = BASE_PATH + "/{" + USER_PLACEHOLDER + "}";

  private final UsersApi usersApi;

  @Transactional(readOnly = true)
  @GetMapping(path = ME_PATH)
  @Operation(description = "Information of the current user if authenticated, ANONYMOUS otherwise")
  public UserResponse getMe(Authentication auth) {
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      return new UserResponse(
          jwtAuth.getName(),
          jwtAuth.getTokenAttributes().getOrDefault(StandardClaimNames.EMAIL, "").toString(),
          jwtAuth.getTokenAttributes().get(StandardClaimNames.PREFERRED_USERNAME).toString(),
          jwtAuth.getTokenAttributes().getOrDefault(StandardClaimNames.GIVEN_NAME, "").toString(),
          jwtAuth.getTokenAttributes().getOrDefault(StandardClaimNames.FAMILY_NAME, "").toString(),
          jwtAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
    return UserResponse.ANONYMOUS;
  }

  @Transactional
  @PostMapping(BASE_PATH)
  public ResponseEntity<Void> createUser(@RequestBody @Valid UserRequest dto) {
    var response = usersApi
        .adminRealmsRealmUsersPost(
            "labs",
            Optional
                .of(
                    new UserRepresentation()
                        .username(dto.username())
                        .firstName(dto.firstName())
                        .lastName(dto.lastName())
                        .email(dto.email())
                        .enabled(true)));
    var pathParts = response.getHeaders().getLocation().getPath().split("/");
    var sub = pathParts[pathParts.length - 1];
    return ResponseEntity
        .created(URI.create(USER_PATH.replace("{%s}".formatted(USER_PLACEHOLDER), sub)))
        .build();
  }
}
