package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import nc.sgcb.labs.commons.validation.IbanString;

public record CardPaymentCreationRequest(
    @NotNull @Pattern(regexp = "[A-Z]{3}") String currency,
    @NotNull @Min(1) Long amount,
    @NotEmpty String cardNumber,
    @NotEmpty @IbanString String destIban) {
}
