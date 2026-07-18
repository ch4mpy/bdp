package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nc.sgcb.labs.commons.validation.IbanString;

public record CardPaymentCreationRequest(
    @NotNull @NotNull String currency,
    @NotNull @Min(1) Long amount,
    @NotEmpty String cardNumber,
    @NotEmpty @IbanString String destIban) {
}
