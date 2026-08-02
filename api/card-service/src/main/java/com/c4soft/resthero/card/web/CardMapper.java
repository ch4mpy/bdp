package com.c4soft.resthero.card.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.c4soft.resthero.card.domain.Card;
import com.c4soft.resthero.commons.domain.IbanStringMapper;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface CardMapper {

  @Mapping(target = "transactionCeiling", source = "ceilings.transaction")
  @Mapping(target = "rolling30Ceiling", source = "ceilings.rolling30")
  @Mapping(target = "isActive", source = "active")
  CardResponse map(Card entity);

}
