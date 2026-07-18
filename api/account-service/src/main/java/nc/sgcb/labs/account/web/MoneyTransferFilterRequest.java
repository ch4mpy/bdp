package nc.sgcb.labs.account.web;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.CurrencyIso3;
import nc.sgcb.labs.commons.validation.IbanString;

/**
 * @param sourceIban a valid IBAN for the source account (optional)
 * @param destinationIban a valid IBAN for the destination account (optional)
 * @param minAmount the minimum amount of the transfer (optional)
 * @param maxAmount the maximum amount of the transfer (optional)
 * @param currencyIso3 a valid ISO 4217 currency code (optional)
 * @param timestampAfter the earliest timestamp of the transfer (optional)
 * @param timestampBefore the latest timestamp of the transfer (optional)
 * @param labelContaining a substring of at least 3 characters that should be contained in the
 *        transfer label (optional)
 */
public record MoneyTransferFilterRequest(
    @Nullable @IbanString String sourceIban,
    @Nullable @IbanString String destinationIban,
    @Nullable @Min(0) Long minAmount,
    @Nullable @Min(0) Long maxAmount,
    @Nullable @CurrencyIso3 String currencyIso3,
    @Nullable Instant timestampAfter,
    @Nullable Instant timestampBefore,
    @Nullable @Size(min = 3) String labelContaining) {
  public static MoneyTransferFilterRequest ALL =
      new MoneyTransferFilterRequest(null, null, null, null, null, null, null, null);
}
