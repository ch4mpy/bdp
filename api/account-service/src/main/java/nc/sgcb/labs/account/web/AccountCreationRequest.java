package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import nc.sgcb.labs.commons.validation.IbanString;

public record AccountCreationRequest(
    @NotNull @IbanString String iban,
    @NotNull String customerId,
    @NotNull @Pattern(regexp = "[A-Z]{3}") String currency) {

}
