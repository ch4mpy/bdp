/**
 *
 */
package com.c4soft.bdp.labs.user.jpa;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import com.c4soft.bdp.labs.user.Role;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
interface RoleJpaRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByLabel(String label);

  Set<Role> findAllByLabelIn(Collection<String> labels);

}
