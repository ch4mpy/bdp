package nc.sgcb.labs.card.payment.web;

import java.time.Instant;
import jakarta.validation.constraints.NotNull;

public record CardPaymentResponse(
    @NotNull Long id,
    @NotNull Instant timestamp,
    @NotNull String currency,
    @NotNull Long amount,
    @NotNull String cardNumber,
    @NotNull String destinationIban,
    boolean isAccepted) {
}
