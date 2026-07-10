package nc.sgcb.labs.account.domain;

import java.time.Instant;
import java.util.Optional;
import nc.sgcb.labs.commons.domain.Iban;

// @formatter:off
public record MoneyTransferFilteringCriteria(
        Optional<Iban> fromIban,
        Optional<Iban> toIban,
        Optional<Long> minAmount,
        Optional<Long> maxAmount,
        Optional<String> currencyIso3,
        Optional<Instant> timestampAfter,
        Optional<Instant> timestampBefore,
        Optional<String> labelContaining) {
// @formatter:on
}
