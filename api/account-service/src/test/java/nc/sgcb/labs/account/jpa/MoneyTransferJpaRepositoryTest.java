package nc.sgcb.labs.account.jpa;

import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.domain.MoneyTransferFilteringCriteria;
import nc.sgcb.labs.commons.domain.Amount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("h2")
class MoneyTransferJpaRepositoryTest {

    @Autowired
    MoneyTransferJpaRepository moneyTransferJpaRepository;

    MoneyTransfer transfer1, transfer2, transfer3;

    @BeforeEach
    void setUp() {
        transfer1 = moneyTransferJpaRepository.save(
                MoneyTransfer.builder()
                        .fromAccountNumber("111-222-333")
                        .toAccountNumber("444-555-666")
                        .amount(new Amount("EUR", 10L))
                        .label("Test transfer 10 EUR")
                        .timestamp(Instant.parse("2025-12-30T12:34:56Z"))
                        .build());
        transfer2 = moneyTransferJpaRepository.save(
                MoneyTransfer.builder()
                        .fromAccountNumber("123-456-789")
                        .toAccountNumber("987-654-321")
                        .amount(new Amount("XPF", 1000L))
                        .label("Test transfer 1000 XPF")
                        .timestamp(Instant.parse("2026-01-23T12:34:56Z"))
                        .build());
        transfer3 = moneyTransferJpaRepository.save(
                MoneyTransfer.builder()
                        .fromAccountNumber("123-456-789")
                        .toAccountNumber("444-555-666")
                        .amount(new Amount("EUR", 2000L))
                        .label("Test transfer 2000 EUR")
                        .timestamp(Instant.parse("2026-06-01T12:34:56Z"))
                        .build());
    }

    @Test
    void whenCriteriaOnFromAccount_thenTransfersFiltered() {
        var actual = moneyTransferJpaRepository.findAll(MoneyTransferJpaRepository.searchSpec(new MoneyTransferFilteringCriteria(
                Optional.of("123-456-789"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        )));
        assertThat(actual).hasSize(2);
        assertThat(actual.stream().allMatch(t -> Objects.equals("123-456-789", t.getFromAccountNumber()))).isTrue();
        assertThat(actual.stream().anyMatch(t -> Objects.equals("987-654-321", t.getToAccountNumber()))).isTrue();
        assertThat(actual.stream().anyMatch(t -> Objects.equals("444-555-666", t.getToAccountNumber()))).isTrue();
    }

    @Test
    void whenCriteriaOnToAccount_thenTransfersFiltered() {
        var actual = moneyTransferJpaRepository.findAll(MoneyTransferJpaRepository.searchSpec(new MoneyTransferFilteringCriteria(
                Optional.empty(),
                Optional.of("444-555-666"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        )));
        assertThat(actual).hasSize(2);
        assertThat(actual.stream().allMatch(t -> Objects.equals("444-555-666", t.getToAccountNumber()))).isTrue();
        assertThat(actual.stream().anyMatch(t -> Objects.equals("111-222-333", t.getFromAccountNumber()))).isTrue();
        assertThat(actual.stream().anyMatch(t -> Objects.equals("123-456-789", t.getFromAccountNumber()))).isTrue();
    }

}
