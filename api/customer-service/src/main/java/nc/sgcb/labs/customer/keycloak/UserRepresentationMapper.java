package nc.sgcb.labs.customer.keycloak;

import org.keycloak.admin.model.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import nc.sgcb.labs.commons.domain.IbanStringMapper;
import nc.sgcb.labs.customer.domain.Customer;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface UserRepresentationMapper {

  Customer map(UserRepresentation entity);

  @Mapping(target = "username", source = "email")
  @Mapping(target = "enabled", expression = "java(true)")
  @Mapping(target = "emailVerified", ignore = true)
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "userProfileMetadata", ignore = true)
  @Mapping(target = "self", ignore = true)
  @Mapping(target = "origin", ignore = true)
  @Mapping(target = "createdTimestamp", ignore = true)
  @Mapping(target = "totp", ignore = true)
  @Mapping(target = "federationLink", ignore = true)
  @Mapping(target = "serviceAccountClientId", ignore = true)
  @Mapping(target = "credentials", ignore = true)
  @Mapping(target = "disableableCredentialTypes", ignore = true)
  @Mapping(target = "requiredActions", ignore = true)
  @Mapping(target = "federatedIdentities", ignore = true)
  @Mapping(target = "realmRoles", ignore = true)
  @Mapping(target = "clientRoles", ignore = true)
  @Mapping(target = "clientConsents", ignore = true)
  @Mapping(target = "notBefore", ignore = true)
  @Mapping(target = "applicationRoles", ignore = true)
  @Mapping(target = "socialLinks", ignore = true)
  @Mapping(target = "groups", ignore = true)
  @Mapping(target = "access", ignore = true)
  UserRepresentation map(Customer dto);
}
