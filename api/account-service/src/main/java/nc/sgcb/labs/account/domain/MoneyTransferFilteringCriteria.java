package nc.sgcb.labs.account.domain;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import nc.sgcb.labs.commons.validation.IbanString;
import nc.sgcb.labs.commons.validation.SupportedCurrency;

public record MoneyTransferFilteringCriteria(
    @Nullable @IbanString String sourceIban,
    @Nullable @IbanString String destinationIban,
    @Nullable Integer minAmount,
    @Nullable Integer maxAmount,
    @Nullable @SupportedCurrency String currency,
    @Nullable Instant timestampAfter,
    @Nullable Instant timestampBefore,
    @Nullable String labelContaining) {
}
