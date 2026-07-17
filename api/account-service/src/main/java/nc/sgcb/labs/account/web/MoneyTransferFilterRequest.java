package nc.sgcb.labs.account.web;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.IbanString;

// @formatter:off
public record MoneyTransferFilterRequest(
        @Nullable @IbanString String fromIban,
        @Nullable @IbanString String toIban,
        @Nullable @Min(0) Long minAmount,
        @Nullable @Min(0) Long maxAmount,
        @Nullable @Pattern(regexp = "\\w{3}")String currencyIso3,
        @Nullable Instant timestampAfter,
        @Nullable Instant timestampBefore,
        @Nullable @Size(min = 3) String labelContaining) {
// @formatter:on
}
