package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.IbanString;
import nc.sgcb.labs.commons.validation.SupportedCurrency;

public record CardPaymentCreationRequest(
    @NotNull @SupportedCurrency String currency,
    @NotNull @Min(1) Integer amount,
    @Size(min = 1, max = 36) String cardNumber,
    @NotNull @IbanString String destinationIban) {
}
