package nc.sgcb.labs.card.payment.jpa;

import nc.sgcb.labs.card.payment.domain.CardPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CardPaymentJpaRepository
    extends JpaRepository<CardPayment, String>, JpaSpecificationExecutor<CardPayment> {

  Page<CardPayment> findByCardNumber(String cardNumber, Pageable pageable);
}
