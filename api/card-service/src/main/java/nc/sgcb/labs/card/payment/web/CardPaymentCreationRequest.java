package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.IbanString;

public record CardPaymentCreationRequest(
    @NotNull @NotNull String currency,
    @NotNull @Min(1) Long amount,
    @Size(min = 1, max = 36) String cardNumber,
    @NotNull @IbanString String destinationIban) {
}
