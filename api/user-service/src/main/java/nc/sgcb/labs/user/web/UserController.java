/**
 *
 */
package nc.sgcb.labs.user.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.commons.exception.InternalServerErrorException;
import nc.sgcb.labs.commons.exception.ResourceNotFoundException;
import nc.sgcb.labs.user.keycloak.KeycloakUserService;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@RestController
@Observed
@Tag(name = "UserPermissions",
    description = "Read roles definitions and edit their assignations to users.")
@RequiredArgsConstructor
public class UserController {

  public static final String ROLES_READ = "roles.read";
  public static final String USER_ROLES_GRANT = "users.roles.grant";
  public static final String USER_ROLES_READ = "users.roles.read";
  public static final String USERS_READ = "users.read";

  private final KeycloakUserService userService;

  private final UserMapper userMapper;

  @GetMapping(path = "/users")
  @PreAuthorize("hasAuthority('" + USERS_READ + "')")
  @Operation(description = "Requires " + USERS_READ + " permission")
  public List<UserResponse> getUsersByUsernameOrEmailContaining(
      @RequestParam String usernameOrEmailContaining, JwtAuthenticationToken auth)
      throws InternalServerErrorException {
    return userService.findByUsernameOrEmailContaining(usernameOrEmailContaining).stream()
        .map(userMapper::map).toList();
  }

  @GetMapping(path = "/users/{sub}")
  @PreAuthorize("hasAuthority('" + USERS_READ + "') || #sub == #auth.getName()")
  @Operation(description = "Requires " + USERS_READ
      + " permission or the accessed user to be the one at the origin of the request")
  public UserResponse getUser(@PathVariable String sub, JwtAuthenticationToken auth)
      throws InternalServerErrorException, ResourceNotFoundException {
    final var user = userService.findBySub(sub);
    return userMapper
        .map(user.orElseThrow(() -> new ResourceNotFoundException("No %s user".formatted(sub))));
  }
}
