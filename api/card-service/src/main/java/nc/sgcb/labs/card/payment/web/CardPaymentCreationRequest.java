package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CardPaymentCreationRequest(
    @NotNull @NotNull String currency,
    @NotNull @Min(1) Long amount,
    @Size(min = 1, max = 36) String cardNumber,
    @NotEmpty String destinationIban) {
}
