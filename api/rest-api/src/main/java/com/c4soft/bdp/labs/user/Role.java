/**
 *
 */
package com.c4soft.bdp.labs.user;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Role implements Serializable {

  private static final long serialVersionUID = 5127096003538373663L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roleSeq")
  @SequenceGenerator(name = "roleSeq", allocationSize = 1, sequenceName = "roles_seq")
  @EqualsAndHashCode.Include
  @ToString.Include
  private @Nullable Long id;

  @Column(nullable = false, unique = true)
  @ToString.Include
  private final String label;

  @ManyToMany(fetch = FetchType.EAGER)
  private Set<Permission> permissions;

  public Role(String label, Set<Permission> permissions) {
    this.label = label;
    this.permissions = Collections.unmodifiableSet(permissions);
  }

  public Role(String label) {
    this(label, Set.of());
  }

  protected Role() {
    this("");
  }

  public Set<Permission> getPermissions() {
    return permissions.stream().collect(Collectors.toSet());
  }

}
