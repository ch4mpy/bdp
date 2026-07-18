package nc.sgcb.labs.user.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.api.UsersApi;
import org.keycloak.admin.model.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import nc.sgcb.labs.commons.exception.CommonExceptionsHandler;
import nc.sgcb.labs.user.SecurityConfig;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserController.class, properties = {})
@Import({CommonExceptionsHandler.class, SecurityConfig.class})
@AutoConfigureAddonsWebmvcResourceServerSecurity
@SuppressWarnings("null")
class UserControllerTest {

  @MockitoBean
  UsersApi usersApi;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper json;

  // ===================== getMe =====================

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenGetMe_thenAnonymousResponse() throws Exception {
    var actual = json
        .readValue(
            mockMvc
                .perform(get("https://localhost" + UserController.ME_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            UserResponse.class);

    assertThat(actual).isEqualTo(UserResponse.ANONYMOUS);
  }

  @Test
  @WithJwt("customer.json")
  void givenJwtAuthenticatedUser_whenGetMe_thenClaimsAndAuthoritiesAreReturned() throws Exception {
    var actual = json
        .readValue(
            mockMvc
                .perform(get("https://localhost" + UserController.ME_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            UserResponse.class);

    assertThat(actual.sub()).isEqualTo("customer-subject");
    assertThat(actual.email()).isEqualTo("john.deuf@mail.pf");
    assertThat(actual.username()).isEqualTo("customer");
    assertThat(actual.firstName()).isEqualTo("John");
    assertThat(actual.lastName()).isEqualTo("Doeuf");
    assertThat(actual.roles()).isNotNull();
  }

  // ===================== createUser =====================

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenCreateUser_thenUnauthorized() throws Exception {
    var dto = new UserRequest("new.user@sgcb.nc", "new-user", "New", "User");

    mockMvc
        .perform(
            post("https://localhost" + UserController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithJwt("customer.json")
  void givenAuthenticatedUserWithoutCreateAuthority_whenCreateUser_thenForbidden()
      throws Exception {
    var dto = new UserRequest("new.user@sgcb.nc", "new-user", "New", "User");

    mockMvc
        .perform(
            post("https://localhost" + UserController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithJwt("user-admin.json")
  void givenUserWithCreateAuthority_whenCreateUser_thenCreatedWithLocationHeader()
      throws Exception {
    var dto = new UserRequest("new.user@sgcb.nc", "new-user", "New", "User");
    var createdUserSub = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    var headers = new HttpHeaders();
    headers
        .setLocation(
            URI.create("https://localhost/admin/realms/labs/users/%s".formatted(createdUserSub)));
    when(usersApi.adminRealmsRealmUsersPost(eq("labs"), any(Optional.class)))
        .thenReturn(new ResponseEntity<Void>(headers, org.springframework.http.HttpStatus.CREATED));

    var mvcResult = mockMvc
        .perform(
            post("https://localhost" + UserController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andReturn();

    assertThat(mvcResult.getResponse().getHeader("Location"))
        .isEqualTo("/users/%s".formatted(createdUserSub));

    var userCaptor = ArgumentCaptor.forClass(Optional.class);
    verify(usersApi).adminRealmsRealmUsersPost(eq("labs"), userCaptor.capture());
    assertThat(userCaptor.getValue()).isPresent();
    var createdUser = (UserRepresentation) userCaptor.getValue().orElseThrow();
    assertThat(createdUser.getUsername()).isEqualTo(dto.username());
    assertThat(createdUser.getFirstName()).isEqualTo(dto.firstName());
    assertThat(createdUser.getLastName()).isEqualTo(dto.lastName());
    assertThat(createdUser.getEmail()).isEqualTo(dto.email());
    assertThat(createdUser.getEnabled()).isTrue();
  }

  @Test
  @WithJwt("user-admin.json")
  void givenInvalidPayload_whenCreateUser_thenUnprocessableEntityAndUpstreamIsNotCalled()
      throws Exception {
    var dto = new UserRequest(null, null, null, null);

    mockMvc
        .perform(
            post("https://localhost" + UserController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isUnprocessableContent());

    verifyNoInteractions(usersApi);
  }

}
