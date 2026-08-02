package com.c4soft.resthero.account.web;

import com.c4soft.resthero.commons.validation.IbanString;
import com.c4soft.resthero.commons.validation.SupportedCurrency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MoneyTransferRequest(
    @NotNull @IbanString String sourceIban,
    @NotNull @IbanString String destinationIban,
    @NotNull @Min(1) Integer amount,
    @NotNull @SupportedCurrency String currency,
    @NotEmpty @Size(min = 3, max = 256) String label) {

}
