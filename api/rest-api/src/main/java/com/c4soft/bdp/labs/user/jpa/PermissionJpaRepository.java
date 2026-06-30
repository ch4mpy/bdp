/**
 *
 */
package com.c4soft.bdp.labs.user.jpa;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import com.c4soft.bdp.labs.user.Permission;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
interface PermissionJpaRepository extends JpaRepository<Permission, Long> {

  Optional<Permission> findByLabel(String label);

  Set<Permission> findAllByLabelIn(Collection<String> label);

}
