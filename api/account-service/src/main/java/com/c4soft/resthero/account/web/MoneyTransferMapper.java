package com.c4soft.resthero.account.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import com.c4soft.resthero.account.domain.MoneyTransfer;
import com.c4soft.resthero.account.domain.MoneyTransferFilteringCriteria;
import com.c4soft.resthero.commons.domain.Amount;
import com.c4soft.resthero.commons.domain.Currency;
import com.c4soft.resthero.commons.domain.IbanStringMapper;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class},
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class MoneyTransferMapper {

  @Autowired
  private IbanStringMapper ibanStringMapper;

  public abstract MoneyTransferFilteringCriteria map(MoneyTransferFilterRequest dto);

  @Mapping(target = "amount", source = "amount.digits")
  @Mapping(target = "currency", source = "amount.currency")
  public abstract MoneyTransferResponse map(MoneyTransfer domain);


  public MoneyTransfer map(MoneyTransferRequest dto) {
    return MoneyTransfer
        .of(
            ibanStringMapper.map(dto.sourceIban()),
            ibanStringMapper.map(dto.destinationIban()),
            new Amount(dto.amount(), Currency.valueOf(dto.currency())),
            dto.label());
  }

}
