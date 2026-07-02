/**
 *
 */
package nc.sgcb.labs.user.web;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import nc.sgcb.labs.user.User;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UserMapper {

  public abstract UserResponse map(User user);

}
