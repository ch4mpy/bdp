package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import nc.sgcb.labs.commons.validation.Iban;

public record AccountCreationRequest(
    @NotNull @Iban String iban,
    @NotNull Long customerId,
    @NotNull @Pattern(regexp = "[A-Z]{3}") String currency) {

}
