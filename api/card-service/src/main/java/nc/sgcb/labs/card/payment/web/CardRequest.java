package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import nc.sgcb.labs.commons.validation.IbanString;

public record CardRequest(
    @NotNull @IbanString String iban,
    @NotNull @Min(1) Integer transactionCeiling,
    @NotNull @Min(1) Integer rolling30Ceiling) {
}
