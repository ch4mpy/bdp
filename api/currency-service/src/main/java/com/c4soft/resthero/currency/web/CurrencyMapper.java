package com.c4soft.resthero.currency.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.c4soft.resthero.commons.domain.Currency;

@Mapper(componentModel = ComponentModel.SPRING)
public interface CurrencyMapper {

  @Mapping(target = "iso3", expression = "java(currency.name())")
  CurrencyResponse map(Currency currency);

}
