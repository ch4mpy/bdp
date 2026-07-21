package nc.sgcb.labs.card.payment.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import nc.sgcb.labs.card.payment.domain.CardPayment;
import nc.sgcb.labs.commons.domain.IbanStringMapper;

@Mapper(componentModel = ComponentModel.SPRING, uses = {IbanStringMapper.class})
public interface CardPaymentMapper {

  @Mapping(target = "amount", source = "amount.digits")
  @Mapping(target = "currency", source = "amount.currencyIso3")
  @Mapping(target = "cardNumber", source = "card.number")
  @Mapping(target = "isAccepted", source = "accepted")
  CardPaymentResponse map(CardPayment entiyy);

}
