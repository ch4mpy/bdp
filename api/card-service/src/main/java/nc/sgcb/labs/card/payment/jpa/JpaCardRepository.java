package nc.sgcb.labs.card.payment.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.commons.domain.Iban;

interface JpaCardRepository extends JpaRepository<Card, String> {

  List<Card> findByIban(Iban iban);
}
