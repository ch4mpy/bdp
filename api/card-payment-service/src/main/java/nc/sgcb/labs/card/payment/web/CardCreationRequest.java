package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.NotNull;
import nc.sgcb.labs.commons.validation.IbanString;

public record CardCreationRequest(
    @NotNull @IbanString String iban,
    @NotNull Long transactionCeiling,
    @NotNull Long rolling30Ceiling) {
}
