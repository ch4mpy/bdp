package nc.sgcb.labs.account.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.domain.MoneyTransferFilteringCriteria;
import nc.sgcb.labs.commons.domain.IbanStringMapper;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class},
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MoneyTransferMapper {

  MoneyTransferFilteringCriteria map(MoneyTransferFilterRequest dto);

  @Mapping(target = "amount", source = "amount.digits")
  @Mapping(target = "currency", source = "amount.currencyIso3")
  MoneyTransferResponse map(MoneyTransfer domain);


  @Mapping(target = "id", ignore = true)
  @Mapping(target = "timestamp", ignore = true)
  @Mapping(target = "amount.digits", source = "amount")
  @Mapping(target = "amount.currencyIso3", source = "currency")
  MoneyTransfer map(MoneyTransferRequest dto);

}
