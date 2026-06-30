/**
 *
 */
package com.c4soft.bdp.labs.user.keycloak;

import java.util.List;
import java.util.Optional;
import org.keycloak.admin.api.UsersApi;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.c4soft.bdp.labs.KeycloakAdminApiProperties;
import com.c4soft.bdp.labs.exception.InternalServerErrorException;
import com.c4soft.bdp.labs.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserService {

  private final KeycloakAdminApiProperties apiProperties;
  private final UsersApi usersApi;
  private final KeycloakUserMapper userMapper;

  public Optional<User> findBySub(String sub) throws InternalServerErrorException {
    log.debug("GET /admin/realms/%s/users/%s?userProfileMetadata=false"
        .formatted(apiProperties.getRealmName(), sub));
    var response = usersApi.adminRealmsRealmUsersUserIdGet(apiProperties.getRealmName(), sub,
        Optional.of(false));
    if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
      return Optional.empty();
    } else if (response.getStatusCode().is2xxSuccessful()) {
      return Optional.ofNullable(response.getBody()).map(userMapper::map);
    }
    throw new InternalServerErrorException(
        "Keycloak admin API responded with status %d when searching for user with %s as subject: %s"
            .formatted(response.getStatusCode(), sub, response.getBody()));
  }

  public Optional<User> findByEmail(String tenant, String email)
      throws InternalServerErrorException {
    log.debug(
        "GET /admin/realms/%s/users?briefRepresentation=false&email=%s&enabled=true&exact=true&idpAlias=%s"
            .formatted(apiProperties.getRealmName(), email, tenant));
    var response = usersApi.adminRealmsRealmUsersGet(apiProperties.getRealmName(),
        Optional.of(false), Optional.empty(), Optional.empty(), Optional.of(email),
        Optional.empty(), Optional.of(true), Optional.of(true), Optional.empty(), Optional.empty(),
        Optional.of(tenant), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
        Optional.empty(), Optional.empty());
    if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
      log.debug("User with %s as email NOT FOUND".formatted(email));
      return Optional.empty();
    } else if (response.getStatusCode().is2xxSuccessful()) {
      if (response.getBody() == null || response.getBody().size() == 0) {
        log.debug("User with %s as email: empty response".formatted(email));
        return Optional.empty();
      }
      if (response.getBody().size() > 1) {
        log.debug("User with %s as email: more than one".formatted(email));
        throw new InternalServerErrorException(
            "Keycloak admin API responded with %d active users when searching for with %s as email: %s"
                .formatted(response.getStatusCode(), email, response.getBody()));
      }
      final var user = response.getBody().get(0);
      return Optional.ofNullable(user).map(userMapper::map);
    }
    throw new InternalServerErrorException(
        "Keycloak admin API responded with status %d when searching for user with %s as email: %s"
            .formatted(response.getStatusCode(), email, response.getBody()));
  }

  public List<User> findByUsernameOrEmailContaining(String usernameOrEmail)
      throws InternalServerErrorException {
    log.debug("GET /admin/realms/%s/users?enabled=true&max=999&search=%s"
        .formatted(apiProperties.getRealmName(), usernameOrEmail));
    var response = usersApi.adminRealmsRealmUsersGet(apiProperties.getRealmName(), Optional.empty(),
        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(true),
        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
        Optional.empty(), Optional.of(999), Optional.empty(), Optional.of(usernameOrEmail),
        Optional.empty());
    if (response.getStatusCode().is2xxSuccessful()) {
      if (response.getBody() == null) {
        log.debug("User with %s as username or email: empty response".formatted(usernameOrEmail));
        return List.of();
      }
      return response.getBody().stream().map(userMapper::map).toList();
    }
    throw new InternalServerErrorException(
        "Keycloak admin API responded with status %d when searching for users with %s as username or email: %s"
            .formatted(response.getStatusCode(), usernameOrEmail, response.getBody()));
  }

}
