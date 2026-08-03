package com.c4soft.resthero.card;

import com.c4soft.resthero.card.domain.Card;
import com.c4soft.resthero.card.domain.Card.Ceilings;
import com.c4soft.resthero.commons.domain.Iban;

public class CardFixtures {

  public static final String CUSTOMER_SUBJECT = "customer-subject";

  public static final String SOMEONE_SUBJECT = "someone-subject";

  public static final String CUSTOMER_IBAN = "FR761111222233334441";

  public static final String SOMEONE_IBAN = "FR761111222233334443";

  public static Card createCustomersCard(Integer transactionCeiling, Integer rolling30Ceiling) {
    var iban = Iban.of(CUSTOMER_IBAN);
    var card = Card.create(
        "4%s0".formatted(iban.getBban()),
        iban,
        Ceilings.builder().transaction(transactionCeiling).rolling30(rolling30Ceiling).build());
    card.activate();
    return card;
  }

  public static Card createSomeonesCard(Integer transactionCeiling, Integer rolling30Ceiling) {
    var iban = Iban.of(SOMEONE_IBAN);
    var card = Card.create(
        "4%s0".formatted(iban.getBban()),
        iban,
        Ceilings.builder().transaction(transactionCeiling).rolling30(rolling30Ceiling).build());
    card.activate();
    return card;
  }
}
