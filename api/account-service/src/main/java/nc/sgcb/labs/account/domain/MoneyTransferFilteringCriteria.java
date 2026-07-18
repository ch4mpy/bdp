package nc.sgcb.labs.account.domain;

import java.time.Instant;
import java.util.Optional;
import nc.sgcb.labs.commons.domain.Iban;

public record MoneyTransferFilteringCriteria(
    Optional<Iban> sourceIban,
    Optional<Iban> destinationIban,
    Optional<Long> minAmount,
    Optional<Long> maxAmount,
    Optional<String> currencyIso3,
    Optional<Instant> timestampAfter,
    Optional<Instant> timestampBefore,
    Optional<String> labelContaining) {
}
