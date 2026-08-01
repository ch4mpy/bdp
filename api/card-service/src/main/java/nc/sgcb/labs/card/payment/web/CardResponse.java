package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.NotNull;

public record CardResponse(
    @NotNull String number,
    @NotNull String iban,
    @NotNull Integer transactionCeiling,
    @NotNull Integer rolling30Ceiling,
    boolean isActive) {

}
