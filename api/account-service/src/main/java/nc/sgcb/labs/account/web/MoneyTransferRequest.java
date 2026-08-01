package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.IbanString;
import nc.sgcb.labs.commons.validation.SupportedCurrency;

public record MoneyTransferRequest(
    @NotNull @IbanString String sourceIban,
    @NotNull @IbanString String destinationIban,
    @NotNull @Min(1) Integer amount,
    @NotNull @SupportedCurrency String currency,
    @NotEmpty @Size(min = 3, max = 256) String label) {

}
