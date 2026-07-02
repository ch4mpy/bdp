package nc.sgcb.labs.user.keycloak;

import org.keycloak.admin.model.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import nc.sgcb.labs.user.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class KeycloakUserMapper {

  @Mapping(target = "sub", source = "id")
  protected abstract User map(UserRepresentation source);

}
