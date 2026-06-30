/**
 *
 */
package com.c4soft.bdp.labs.user;

import java.io.Serializable;
import org.jspecify.annotations.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Entity
@Table(name = "permissions")
@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permission implements Serializable {

  private static final long serialVersionUID = -4039192719013349822L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permissionSeq")
  @SequenceGenerator(name = "permissionSeq", allocationSize = 1, sequenceName = "permissions_seq")
  @EqualsAndHashCode.Include
  private @Nullable Long id;

  @Column(nullable = false, unique = true)
  private final String label;

  public Permission(String label) {
    this.label = label;
  }

  protected Permission() {
    this("");
  }

}
