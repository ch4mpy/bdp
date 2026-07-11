package nc.sgcb.labs.card.payment.jpa;

import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.card.payment.domain.CardPayment;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("h2")
class CardPaymentJpaRepositoryTest {

  @Autowired
  CardPaymentJpaRepository paymentRepo;

  @Autowired
  CardJpaRepository cardRepo;

  Iban iban = Iban.parse("FR761111222233334444");

  @SuppressWarnings("null")
  Card card1, card2;

  @SuppressWarnings("null")
  CardPayment payment1, payment2, payment3;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    card1 = cardRepo.save(Card
        .builder()
        .number("1111-1111-1111-1111-1")
        .iban(iban)
        .ceilings(Card.Ceilings.builder().rolling30(300000L).transaction(100000L).build())
        .build());
    card2 = cardRepo.save(Card
        .builder()
        .number("2222-2222-2222-2222-2")
        .iban(iban)
        .ceilings(Card.Ceilings.builder().rolling30(500000L).transaction(150000L).build())
        .build());

    payment1 = paymentRepo.save(CardPayment
        .builder()
        .card(card1)
        .amount(new Amount("XPF", 120000L))
        .destIban(Iban.parse("FR7622222222222222222"))
        .isAccepted(false)
        .timestamp(Instant.parse("2026-01-01T00:01:10Z"))
        .build());
    payment2 = paymentRepo.save(CardPayment
        .builder()
        .card(card1)
        .amount(new Amount("XPF", 120000L))
        .destIban(Iban.parse("FR7622222222222222222"))
        .isAccepted(false)
        .timestamp(Instant.parse("2026-01-01T00:01:30Z"))
        .build());
    payment3 = paymentRepo.save(CardPayment
        .builder()
        .card(card2)
        .amount(new Amount("XPF", 120000L))
        .destIban(Iban.parse("FR7622222222222222222"))
        .timestamp(Instant.parse("2026-01-01T00:02:30Z"))
        .isAccepted(true)
        .build());
  }

  @Test
  void whenFindByCardNumber_thenFiltered() {
    final var actual = paymentRepo.findByCardNumber("1111-1111-1111-1111-1", PageRequest.of(0, 10));
    assertThat(actual.getContent()).containsExactlyInAnyOrder(payment1, payment1);
  }

  @Test
  @SuppressWarnings("null")
  void whenFindByCardNumberAndTimestampBetween_thenFiltered() {
    final var actual = paymentRepo.findByCardNumberAndTimestampBetween(
        "1111-1111-1111-1111-1",
        Instant.parse("2026-01-01T00:01:00Z"),
        Instant.parse("2026-01-01T00:01:15Z"));
    assertThat(actual).containsExactlyInAnyOrder(payment1);

  }
}
