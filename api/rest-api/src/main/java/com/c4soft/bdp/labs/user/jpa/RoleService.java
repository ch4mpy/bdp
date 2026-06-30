/**
 *
 */
package com.c4soft.bdp.labs.user.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.c4soft.bdp.labs.user.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A UserRepo proxy to add caching
 *
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Service
@CacheConfig(cacheNames = "roles")
@RequiredArgsConstructor
@Slf4j
public class RoleService {

  private final RoleJpaRepository roleRepo;

  @Cacheable
  public Optional<Role> findByLabel(String label) {
    log.debug("Querying for %s role".formatted(label));
    return roleRepo.findByLabel(label);
  }

  @Cacheable(key = "'all'")
  public List<Role> findAll() {
    log.debug("Querying all roles");
    return roleRepo.findAll();
  }

  @Caching(put = @CachePut(key = "#role.id"), evict = @CacheEvict(
		  key = "'all'"))
  public Role save(Role role) {
    log.debug("Saving role %d: %s".formatted(role.getId(), role.getLabel()));
    return roleRepo.save(role);
  }


  @Caching( evict = { @CacheEvict(
		  key = "'#role.id'"), @CacheEvict(
				  key = "'all'") })
  public void delete(Role role) {
    log.debug("Querying all roles");
    roleRepo.delete(role);
  }


}
