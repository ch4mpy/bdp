package com.c4soft.resthero.account.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.c4soft.resthero.account.domain.Account;
import com.c4soft.resthero.commons.domain.IbanStringMapper;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface AccountMapper {

  @Mapping(target = "balance", source = "balance.digits")
  @Mapping(target = "currency", source = "balance.currency")
  AccountResponse map(Account account);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "balance.currency", source = "currency")
  Account map(AccountCreationRequest dto);
}
