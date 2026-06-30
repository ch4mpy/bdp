/**
 *
 */
package com.c4soft.bdp.labs.user;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class UserRoles implements Serializable {

  private static final long serialVersionUID = -7352198924952784634L;

  @Id
  private String userSub;

  @ManyToMany(cascade = CascadeType.ALL)
  private Set<Role> roles;

  public UserRoles(String userSub, Set<Role> roles) {
    this.userSub = userSub;
    this.roles = new HashSet<>(roles);
  }

  protected UserRoles() {
    this("", Set.of());
  }

  public Set<Permission> getPermissions() {
    return roles.stream().flatMap(r -> r.getPermissions().stream()).collect(Collectors.toSet());
  }
}
