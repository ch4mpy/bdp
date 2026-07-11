package nc.sgcb.labs.account.web;

import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.commons.domain.IbanStringMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface AccountMapper {

  @Mapping(target = "balance", source = "balance.digits")
  @Mapping(target = "currency", source = "balance.currencyIso3")
  AccountResponse map(Account account);

}
