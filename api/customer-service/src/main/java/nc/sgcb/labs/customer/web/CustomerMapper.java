package nc.sgcb.labs.customer.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import nc.sgcb.labs.commons.domain.IbanStringMapper;
import nc.sgcb.labs.customer.domain.Beneficiary;
import nc.sgcb.labs.customer.domain.Customer;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface CustomerMapper {

  CustomerResponse map(Customer entity);

  @Mapping(target = "beneficiaries", ignore = true)
  Customer map(CustomerCreationRequest dto, String id);

  BeneficiaryResponse map(Beneficiary entity);

  @Mapping(target = "id", ignore = true)
  Beneficiary map(BeneficiaryRequest dto, Customer customer);

  @Mapping(target = "id", ignore = true)
  Beneficiary map(@MappingTarget Beneficiary entity, BeneficiaryRequest dto, Customer customer);
}
