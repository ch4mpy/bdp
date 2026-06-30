package com.c4soft.bdp.labs.user.keycloak;

import org.keycloak.admin.model.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import com.c4soft.bdp.labs.user.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class KeycloakUserMapper {

  @Mapping(target = "sub", source = "id")
  protected abstract User map(UserRepresentation source);

}
