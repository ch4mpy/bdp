package nc.sgcb.labs.customer.web;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import nc.sgcb.labs.commons.domain.IbanStringMapper;
import nc.sgcb.labs.customer.domain.Customer;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface CustomerMapper {

  CustomerResponse map(Customer entity);

  Customer map(CustomerCreationRequest dto, String id);

}
