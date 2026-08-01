package nc.sgcb.labs.account.domain;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import nc.sgcb.labs.commons.domain.Currency;
import nc.sgcb.labs.commons.domain.Iban;

public record MoneyTransferFilteringCriteria(
    @Nullable Iban sourceIban,
    @Nullable Iban destinationIban,
    @Nullable Integer minAmount,
    @Nullable Integer maxAmount,
    @Nullable Currency currency,
    @Nullable Instant timestampAfter,
    @Nullable Instant timestampBefore,
    @Nullable String labelContaining) {
}
