package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CardCeilingsRequest(
    @NotNull @Min(1) Long transactionCeiling,
    @NotNull @Min(1) Long rolling30Ceiling) {

}
