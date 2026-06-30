/**
 *
 */
package com.c4soft.bdp.labs.user.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import com.c4soft.bdp.labs.user.UserRoles;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
interface UserRolesJpaRepository extends JpaRepository<UserRoles, String> {
}
