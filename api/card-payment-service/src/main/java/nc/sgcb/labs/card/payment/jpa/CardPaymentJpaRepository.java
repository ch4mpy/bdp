package nc.sgcb.labs.card.payment.jpa;

import nc.sgcb.labs.card.payment.domain.CardPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CardPaymentJpaRepository extends JpaRepository<CardPayment, Long> {

  Page<CardPayment> findByCardNumber(String cardNumber, Pageable pageable);

  List<CardPayment> findByCardNumberAndTimestampBetween(
      String cardNumber,
      Instant from,
      Instant to);
}
