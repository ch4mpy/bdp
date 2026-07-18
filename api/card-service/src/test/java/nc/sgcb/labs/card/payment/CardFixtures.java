package nc.sgcb.labs.card.payment;

import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.card.payment.domain.Card.Ceilings;
import nc.sgcb.labs.commons.domain.Iban;

public class CardFixtures {

  public static final String CUSTOMER_SUBJECT = "customer-subject";

  public static final String SOMEONE_SUBJECT = "someone-subject";

  public static final String CUSTOMER_IBAN = "FR761111222233334441";

  public static final String SOMEONE_IBAN = "FR761111222233334443";

  public static Card createCustomersCard(Long transactionCeiling, Long rolling30Ceiling) {
    var iban = Iban.of(CUSTOMER_IBAN);
    return Card
        .builder()
        .number("4%s0".formatted(iban.getBban()))
        .iban(iban)
        .ceilings(
            Ceilings.builder().transaction(transactionCeiling).rolling30(rolling30Ceiling).build())
        .active(true)
        .build();
  }

  public static Card createSomeonesCard(Long transactionCeiling, Long rolling30Ceiling) {
    var iban = Iban.of(SOMEONE_IBAN);
    return Card
        .builder()
        .number("4%s0".formatted(iban.getBban()))
        .iban(iban)
        .ceilings(
            Ceilings.builder().transaction(transactionCeiling).rolling30(rolling30Ceiling).build())
        .active(true)
        .build();
  }
}
