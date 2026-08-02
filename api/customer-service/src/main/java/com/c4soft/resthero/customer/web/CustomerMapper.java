package com.c4soft.resthero.customer.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.c4soft.resthero.commons.domain.IbanStringMapper;
import com.c4soft.resthero.customer.domain.Beneficiary;
import com.c4soft.resthero.customer.domain.Customer;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface CustomerMapper {

  CustomerResponse map(Customer entity);

  @Mapping(target = "id", ignore = true)
  Customer map(CustomerCreationRequest dto);

  BeneficiaryResponse map(Beneficiary entity);

  @Mapping(target = "id", ignore = true)
  Beneficiary map(BeneficiaryRequest dto, String customerId);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "customerId", ignore = true)
  Beneficiary map(@MappingTarget Beneficiary entity, BeneficiaryRequest dto);
}
