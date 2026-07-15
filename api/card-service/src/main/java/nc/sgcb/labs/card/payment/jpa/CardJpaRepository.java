package nc.sgcb.labs.card.payment.jpa;

import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.commons.domain.Iban;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardJpaRepository extends JpaRepository<Card, String> {

  List<Card> findByIban(Iban iban);
}
