package nc.sgcb.labs.account.domain;

import java.time.Instant;
import java.util.Optional;

// @formatter:off
public record MoneyTransferFilteringCriteria(
        Optional<String> fromAccountNumber,
        Optional<String> toAccountNumber,
        Optional<Long> minAmount,
        Optional<Long> maxAmount,
        Optional<String> currencyIso3,
        Optional<Instant> timestampAfter,
        Optional<Instant> timestampBefore,
        Optional<String> labelContaining) {
// @formatter:on
}
