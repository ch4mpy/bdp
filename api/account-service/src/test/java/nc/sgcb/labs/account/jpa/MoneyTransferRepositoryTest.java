package nc.sgcb.labs.account.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.domain.MoneyTransferFilteringCriteria;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Currency;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.domain.IbanStringMapperImpl;

@DataJpaTest
@ActiveProfiles("h2")
@Import({IbanStringMapperImpl.class})
class MoneyTransferRepositoryTest {

  @Autowired
  MoneyTransferRepository moneyTransferJpaRepository;

  MoneyTransfer transfer1, transfer2, transfer3;

  @BeforeEach
  void setUp() {
    transfer1 = moneyTransferJpaRepository
        .save(
            MoneyTransfer
                .builder()
                .sourceIban(Iban.of("FR76 111222333"))
                .destinationIban(Iban.of("FR76 444555666"))
                .amount(Amount.builder().digits(1000).currency(Currency.XPF).build())
                .label("Test transfer 1000 XPF")
                .timestamp(Instant.parse("2025-12-30T12:34:56Z"))
                .build());
    transfer2 = moneyTransferJpaRepository
        .save(
            MoneyTransfer
                .builder()
                .sourceIban(Iban.of("FR76 123456789"))
                .destinationIban(Iban.of("FR76 987654321"))
                .amount(Amount.builder().digits(2000).currency(Currency.EUR).build())
                .label("Test transfer 20 EUR")
                .timestamp(Instant.parse("2026-01-23T12:34:56Z"))
                .build());
    transfer3 = moneyTransferJpaRepository
        .save(
            MoneyTransfer
                .builder()
                .sourceIban(Iban.of("FR76 123456789"))
                .destinationIban(Iban.of("FR76 444555666"))
                .amount(Amount.builder().digits(3000).currency(Currency.AUD).build())
                .label("Test transfer 3 AUD")
                .timestamp(Instant.parse("2026-06-01T12:34:56Z"))
                .build());
  }

  @Test
  void whenCriteriaOnFromAccount_thenTransfersFiltered() {
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        "FR76 123456789",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null)));
    assertThat(actual).hasSize(2);
    assertThat(
        actual.stream().allMatch(t -> Objects.equals(Iban.of("FR76 123456789"), t.getSourceIban())))
        .isTrue();
    assertThat(
        actual
            .stream()
            .anyMatch(t -> Objects.equals(Iban.of("FR76 987654321"), t.getDestinationIban())))
        .isTrue();
    assertThat(
        actual
            .stream()
            .anyMatch(t -> Objects.equals(Iban.of("FR76 444555666"), t.getDestinationIban())))
        .isTrue();
  }

  @Test
  void whenCriteriaOnToAccount_thenTransfersFiltered() {
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        null,
                        "FR76 444555666",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null)));
    assertThat(actual).hasSize(2);
    assertThat(
        actual
            .stream()
            .allMatch(t -> Objects.equals(Iban.of("FR76 444555666"), t.getDestinationIban())))
        .isTrue();
    assertThat(
        actual.stream().anyMatch(t -> Objects.equals(Iban.of("FR76 111222333"), t.getSourceIban())))
        .isTrue();
    assertThat(
        actual.stream().anyMatch(t -> Objects.equals(Iban.of("FR76 123456789"), t.getSourceIban())))
        .isTrue();
  }

  @Test
  void whenCriteriaOnMinAmount_thenTransfersFiltered() {
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        null,
                        null,
                        2000,
                        null,
                        null,
                        null,
                        null,
                        null)));
    assertThat(actual).hasSize(2);
    assertThat(actual.stream().anyMatch(t -> Objects.equals(2000, t.getAmount().getDigits())))
        .isTrue();
    assertThat(actual.stream().anyMatch(t -> Objects.equals(3000, t.getAmount().getDigits())))
        .isTrue();
  }

  @Test
  void whenCriteriaOnMaxAmount_thenTransfersFiltered() {
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        null,
                        null,
                        null,
                        2000,
                        null,
                        null,
                        null,
                        null)));
    assertThat(actual).hasSize(2);
    assertThat(actual.stream().anyMatch(t -> Objects.equals(1000, t.getAmount().getDigits())))
        .isTrue();
    assertThat(actual.stream().anyMatch(t -> Objects.equals(2000, t.getAmount().getDigits())))
        .isTrue();
  }

  @Test
  void whenCriteriaOnCurrency_thenTransfersFiltered() {
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        null,
                        null,
                        null,
                        null,
                        "XPF",
                        null,
                        null,
                        null)));
    assertThat(actual).hasSize(1);
    assertThat(
        actual.stream().allMatch(t -> Objects.equals(Currency.XPF, t.getAmount().getCurrency())))
        .isTrue();
  }

  @Test
  void whenCriteriaOnMinTimestamp_thenTransfersFiltered() {
    final var from = Instant.parse("2026-01-01T00:00:00Z");
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        from,
                        null,
                        null)));
    assertThat(actual).hasSize(2);
    assertThat(actual.stream().allMatch(t -> t.getTimestamp().isAfter(from))).isTrue();
  }

  @Test
  void whenCriteriaOnMaxTimestamp_thenTransfersFiltered() {
    final var to = Instant.parse("2026-01-31T23:59:59Z");
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        to,
                        null)));
    assertThat(actual).hasSize(2);
    assertThat(actual.stream().allMatch(t -> t.getTimestamp().isBefore(to))).isTrue();
  }

  @Test
  void whenCriteriaOnLabel_thenTransfersFiltered() {
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "3")));
    assertThat(actual).hasSize(1);
    assertThat(actual.stream().allMatch(t -> t.getLabel().contains("3"))).isTrue();
  }

  @Test
  void whenAllCriteriaSet_thenTransfersFiltered() {
    var instant = Instant.parse("2025-12-30T12:34:56Z");
    var actual = moneyTransferJpaRepository
        .findAll(
            MoneyTransferRepository
                .searchSpec(
                    new MoneyTransferFilteringCriteria(
                        "FR76 111222333",
                        "FR76 444555666",
                        1000,
                        1000,
                        "XPF",
                        instant,
                        instant,
                        "1000")));
    assertThat(actual).hasSize(1);
  }

}
