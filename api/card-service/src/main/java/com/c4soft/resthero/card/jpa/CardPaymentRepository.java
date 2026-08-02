package com.c4soft.resthero.card.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.c4soft.resthero.card.domain.CardPayment;
import java.time.Instant;
import java.util.List;

public interface CardPaymentRepository extends JpaRepository<CardPayment, Long> {

  Page<CardPayment> findByCardNumber(String cardNumber, Pageable pageable);

  List<CardPayment> findByCardNumberAndTimestampBetween(
      String cardNumber,
      Instant from,
      Instant to);
}
