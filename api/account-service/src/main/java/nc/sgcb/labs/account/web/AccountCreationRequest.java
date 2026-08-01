package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.SupportedCurrency;
import nc.sgcb.labs.commons.validation.IbanString;

public record AccountCreationRequest(
    @NotNull @IbanString String iban,
    @NotNull @Size(min = 1, max = 36) String customerId,
    @NotNull @SupportedCurrency String currency) {

}
