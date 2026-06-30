/**
 *
 */
package com.c4soft.bdp.labs.user.web;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.c4soft.bdp.labs.exception.InternalServerErrorException;
import com.c4soft.bdp.labs.exception.ResourceNotFoundException;
import com.c4soft.bdp.labs.user.Permission;
import com.c4soft.bdp.labs.user.jpa.RoleService;
import com.c4soft.bdp.labs.user.jpa.UserRolesService;
import com.c4soft.bdp.labs.user.keycloak.KeycloakUserService;
import com.c4soft.bdp.labs.user.web.RoleMapper.RoleNotFoundException;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@RestController
@Observed
@Tag(name = "UserPermissions",
    description = "Read roles definitions and edit their assignations to users.")
@RequiredArgsConstructor
public class UserRolesController {

  public static final String ROLES_READ = "roles.read";
  public static final String USER_ROLES_GRANT = "users.roles.grant";
  public static final String USER_ROLES_READ = "users.roles.read";
  public static final String USERS_READ = "users.read";

  private final RoleService roleService;

  private final RoleMapper roleMapper;

  private final KeycloakUserService userService;

  private final UserRolesService userRolesService;

  private final UserMapper userMapper;

  @GetMapping(path = "/roles")
  @PreAuthorize("hasAuthority('" + ROLES_READ + "')")
  @Operation(description = "Requires " + ROLES_READ + " permission")
  @Transactional(readOnly = true)
  public List<RoleResponse> getAllRoles() {
    return roleService.findAll().stream().map(r -> new RoleResponse(r.getLabel(),
        r.getPermissions().stream().map(Permission::getLabel).toList())).toList();
  }

  @GetMapping(path = "/users")
  @PreAuthorize("hasAuthority('" + USERS_READ + "')")
  @Operation(description = "Requires " + USERS_READ + " permission")
  @Transactional(readOnly = true)
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
  @Transactional(readOnly = true)
  public UserResponse getUser(@PathVariable String sub, JwtAuthenticationToken auth)
      throws InternalServerErrorException, ResourceNotFoundException {
    final var user = userService.findBySub(sub);
    return userMapper
        .map(user.orElseThrow(() -> new ResourceNotFoundException("No %s user".formatted(sub))));
  }

  @GetMapping(path = "/users/{sub}/roles")
  @PreAuthorize("hasAuthority('" + USER_ROLES_READ + "') || #sub == #auth.getName()")
  @Operation(description = "Requires " + USER_ROLES_READ
      + " permission or the accessed user to be the one at the origin of the request")
  @Transactional(readOnly = true)
  public List<RoleResponse> getUserTenantRoles(@PathVariable String sub,
      JwtAuthenticationToken auth) {
    final var roles = userRolesService.findBySub(sub);
    return roles.stream().map(roleMapper::map).toList();
  }

  @PutMapping(path = "/users/{sub}/roles")
  @PreAuthorize("hasAuthority('" + USER_ROLES_GRANT + "')")
  @Operation(description = "Requires " + USER_ROLES_GRANT + " permission")
  @Transactional(readOnly = false)
  public ResponseEntity<Void> setUserTenantRoles(@PathVariable String sub,
      @RequestBody UserRolesRequest dto, JwtAuthenticationToken auth) throws RoleNotFoundException {
    final var roles = dto.roleLabels().stream().map(roleService::findByLabel)
        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    userRolesService.save(sub, roles);
    return ResponseEntity.accepted().build();
  }
}
