/**
 *
 */
package com.c4soft.bdp.labs.user.web;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import com.c4soft.bdp.labs.user.User;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RoleMapper.class})
public abstract class UserMapper {

  public abstract UserResponse map(User user);

}
