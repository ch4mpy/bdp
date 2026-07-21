package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.CurrencyIso3;
import nc.sgcb.labs.commons.validation.IbanString;

public record MoneyTransferRequest(
    @NotNull @IbanString String sourceIban,
    @NotNull @IbanString String destinationIban,
    @NotNull @Min(1) Long amount,
    @NotNull @CurrencyIso3 String currency,
    @NotEmpty @Size(min = 3, max = 256) String label) {

}
