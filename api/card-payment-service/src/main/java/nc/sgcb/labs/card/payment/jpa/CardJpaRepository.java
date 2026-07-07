package nc.sgcb.labs.card.payment.jpa;

import nc.sgcb.labs.card.payment.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardJpaRepository extends JpaRepository<Card, String> {
}
