package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.NotNull;
import nc.sgcb.labs.commons.validation.Iban;

public record CardCreationRequest(
    @NotNull @Iban String iban,
    @NotNull Long transactionCeiling,
    @NotNull Long rolling30Ceiling) {
}
