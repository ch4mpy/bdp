package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.CurrencyIso3;
import nc.sgcb.labs.commons.validation.IbanString;

public record AccountCreationRequest(
    @NotNull @IbanString String iban,
    @NotNull @Size(min = 1, max = 36) String customerId,
    @NotNull @CurrencyIso3 String currency) {

}
