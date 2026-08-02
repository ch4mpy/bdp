package com.c4soft.resthero.account.domain;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import com.c4soft.resthero.commons.domain.Currency;
import com.c4soft.resthero.commons.domain.Iban;

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
