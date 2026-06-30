package com.c4soft.bdp.labs.user.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.MockMvcSupport;
import com.c4soft.bdp.labs.exception.ExceptionsHandler;
import com.c4soft.bdp.labs.user.Permission;
import com.c4soft.bdp.labs.user.Role;
import com.c4soft.bdp.labs.user.jpa.RoleService;
import com.c4soft.bdp.labs.user.jpa.UserRolesService;
import com.c4soft.bdp.labs.user.keycloak.KeycloakUserService;

@WebMvcTest(controllers = UserRolesController.class, properties = {})
@AutoConfigureAddonsWebmvcResourceServerSecurity
@EnableMethodSecurity
@Import({ExceptionsHandler.class, RoleMapperImpl.class, UserMapperImpl.class})
class UserRolesControllerTest {
  @Autowired
  MockMvcSupport api;

  @MockitoBean
  RoleService roleService;

  @Autowired
  RoleMapper roleMapper;

  @MockitoBean
  KeycloakUserService userService;

  @MockitoBean
  UserRolesService userRolesService;

  @Autowired
  UserMapper userMapper;

  private final Role managerRole = new Role("manager",
      Set.of(new Permission(1L, UserRolesController.ROLES_READ),
          new Permission(2L, UserRolesController.USERS_READ),
          new Permission(3L, UserRolesController.USER_ROLES_READ),
          new Permission(4L, UserRolesController.USER_ROLES_GRANT)));

  private final Role workerRole =
      new Role("worker", Set.of(new Permission(1L, UserRolesController.ROLES_READ)));

  @Test
  void givenRequestIsAnonymous_whenGetAllRoles_thenUnauthorized() throws Exception {
    api.get("https://localhost/roles").andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockAuthentication
  void givenUserDoesNotHaveTheRolesReadPermission_whenGetAllRoles_thenForbidden() throws Exception {
    api.get("https://localhost/roles").andExpect(status().isForbidden());
  }

  @Test
  @WithMockAuthentication({UserRolesController.ROLES_READ})
  void givenUserHasTheRolesReadPermission_whenGetAllRoles_thenOk() throws Exception {
    api.get("https://localhost/roles").andExpect(status().isOk());
  }

}
