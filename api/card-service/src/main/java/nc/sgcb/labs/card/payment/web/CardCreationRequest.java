package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import nc.sgcb.labs.commons.validation.IbanString;

public record CardCreationRequest(
    @NotNull @IbanString String iban,
    @NotNull @Min(1) Long transactionCeiling,
    @NotNull @Min(1) Long rolling30Ceiling) {
}
