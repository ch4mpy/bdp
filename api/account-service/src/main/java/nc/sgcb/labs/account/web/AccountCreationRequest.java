package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nc.sgcb.labs.commons.validation.CurrencyIso3;
import nc.sgcb.labs.commons.validation.IbanString;

public record AccountCreationRequest(
    @NotNull @IbanString String iban,
    @NotNull @NotEmpty String customerId,
    @NotNull @CurrencyIso3 String currency) {

}
