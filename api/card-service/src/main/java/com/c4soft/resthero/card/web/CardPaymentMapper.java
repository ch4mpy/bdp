package com.c4soft.resthero.card.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import com.c4soft.resthero.card.domain.CardPayment;
import com.c4soft.resthero.commons.domain.IbanStringMapper;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface CardPaymentMapper {

  @Mapping(target = "amount", source = "amount.digits")
  @Mapping(target = "currency", source = "amount.currency")
  @Mapping(target = "cardNumber", source = "card.number")
  @Mapping(target = "isAccepted", source = "accepted")
  CardPaymentResponse map(CardPayment entiyy);

}
