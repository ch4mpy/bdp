/**
 *
 */
package com.c4soft.bdp.labs.user.web;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;
import com.c4soft.bdp.labs.user.Permission;
import com.c4soft.bdp.labs.user.Role;
import com.c4soft.bdp.labs.user.jpa.RoleService;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class RoleMapper {

  @Autowired
  RoleService roleRepo;

  public RoleResponse map(Role domain) {
    return new RoleResponse(domain.getLabel(),
        domain.getPermissions().stream().map(Permission::getLabel).toList());
  }

  public Role map(String dto) throws RoleNotFoundException {
    return roleRepo.findByLabel(dto).orElseThrow(() -> new RoleNotFoundException(dto));
  }

  public static class RoleNotFoundException extends Exception {
    private static final long serialVersionUID = 6016538465254032132L;

    public RoleNotFoundException(String label) {
      super("No role with label %s".formatted(label));
    }
  }
}
