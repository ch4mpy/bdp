package nc.sgcb.labs.currency.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import nc.sgcb.labs.commons.domain.Currency;

@Mapper(componentModel = ComponentModel.SPRING)
public interface CurrencyMapper {

  @Mapping(target = "iso3", expression = "java(currency.name())")
  CurrencyResponse map(Currency currency);

}
