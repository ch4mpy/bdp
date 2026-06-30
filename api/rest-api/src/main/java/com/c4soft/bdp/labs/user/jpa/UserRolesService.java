/**
 *
 */
package com.c4soft.bdp.labs.user.jpa;

import java.util.Set;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.c4soft.bdp.labs.user.Role;
import com.c4soft.bdp.labs.user.UserRoles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Service
@CacheConfig(cacheNames = "user-roles")
@RequiredArgsConstructor
@Slf4j
public class UserRolesService {

  private final UserRolesJpaRepository userTenantRolesJpaRepository;

  @Cacheable(key = "#userSub")
  public Set<Role> findBySub(String userSub) {
    log.debug("Querying roles for user %s".formatted(userSub));
    var entity =
        userTenantRolesJpaRepository.findById(userSub);
    return entity.map(UserRoles::getRoles).orElse(Set.of());
  }

  @CachePut(key = "#userSub")
  public Set<Role> save(String userSub, Set<Role> roles) {
    log.debug("Setting roles %s for user %s".formatted(roles, userSub));
    var userRoles =
        userTenantRolesJpaRepository.findById(userSub);
    userRoles.ifPresent(utr -> utr.setRoles(roles));

    userTenantRolesJpaRepository.save(
        userRoles.orElseGet(() -> new UserRoles(userSub, roles)));

    return userTenantRolesJpaRepository.save(
            userRoles.orElseGet(() -> new UserRoles(userSub, roles))).getRoles();
  }

}
