package nc.sgcb.labs.account.web;

import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.domain.MoneyTransferFilteringCriteria;
import nc.sgcb.labs.commons.domain.IbanStringMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface MoneyTransferMapper {

  MoneyTransferFilteringCriteria map(MoneyTransferFilterRequest dto);

  @Mapping(target = "amount", source = "amount.digits")
  @Mapping(target = "currency", source = "amount.currencyIso3")
  MoneyTransferResponse map(MoneyTransfer domain);

}
