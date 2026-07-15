package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.NotNull;

public record CardResponse(
    @NotNull String number,
    @NotNull String iban,
    @NotNull Long transactionCeiling,
    @NotNull Long rolling30Ceiling,
    boolean isActive) {

}
