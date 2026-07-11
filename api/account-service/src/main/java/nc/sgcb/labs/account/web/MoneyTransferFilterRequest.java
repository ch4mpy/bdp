package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

// @formatter:off
public record MoneyTransferFilterRequest(
        @Nullable @Pattern(regexp = "\\d+") String fromIban,
        @Nullable @Pattern(regexp = "\\d+") String toIban,
        @Nullable @Min(0) Long minAmount,
        @Nullable @Min(0) Long maxAmount,
        @Nullable @Pattern(regexp = "\\w{3}")String currencyIso3,
        @Nullable Instant timestampAfter,
        @Nullable Instant timestampBefore,
        @Nullable @Size(min = 3) String labelContaining) {
// @formatter:on
}
