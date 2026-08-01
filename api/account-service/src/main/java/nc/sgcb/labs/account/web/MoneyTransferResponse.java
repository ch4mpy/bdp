package nc.sgcb.labs.account.web;

import java.time.Instant;
import jakarta.validation.constraints.NotNull;

/**
 * @param amount In minor unit (i.e. 1000 for 1000 XPF, 10.00 USD, 1.000 KWD)
 * @param currency in ISO_3 format
 */
public record MoneyTransferResponse(
    @NotNull String sourceIban,
    @NotNull String destinationIban,
    @NotNull Integer amount,
    @NotNull String currency,
    @NotNull Instant timestamp,
    @NotNull String label) {

}
