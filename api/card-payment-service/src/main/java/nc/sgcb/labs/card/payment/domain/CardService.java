package nc.sgcb.labs.card.payment.domain;

import java.util.List;
import java.util.Optional;
import nc.sgcb.labs.commons.domain.Iban;

public interface CardService {

  Optional<Card> findByNumber(String number);

  List<Card> findByIban(Iban iban);

  Card save(Card card);
}
