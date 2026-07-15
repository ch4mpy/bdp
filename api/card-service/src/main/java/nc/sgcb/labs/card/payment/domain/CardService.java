package nc.sgcb.labs.card.payment.domain;

import nc.sgcb.labs.commons.domain.Iban;

import java.util.List;
import java.util.Optional;

public interface CardService {

  Optional<Card> findByNumber(String number);

  List<Card> findByIban(Iban iban);

  Card save(Card card);
}
