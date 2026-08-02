package com.c4soft.resthero.account.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;
import com.c4soft.resthero.account.domain.MoneyTransfer;
import com.c4soft.resthero.account.domain.MoneyTransferFilteringCriteria;
import com.c4soft.resthero.commons.domain.IbanStringMapper;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class},
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MoneyTransferMapper {

  MoneyTransferFilteringCriteria map(MoneyTransferFilterRequest dto);

  @Mapping(target = "amount", source = "amount.digits")
  @Mapping(target = "currency", source = "amount.currency")
  MoneyTransferResponse map(MoneyTransfer domain);


  @Mapping(target = "id", ignore = true)
  @Mapping(target = "timestamp", ignore = true)
  @Mapping(target = "amount.digits", source = "amount")
  @Mapping(target = "amount.currency", source = "currency")
  MoneyTransfer map(MoneyTransferRequest dto);


}
