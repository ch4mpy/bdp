package com.c4soft.resthero.account.web;

import com.c4soft.resthero.commons.validation.IbanString;
import com.c4soft.resthero.commons.validation.SupportedCurrency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountCreationRequest(
    @NotNull @IbanString String iban,
    @NotNull @Size(min = 1, max = 36) String customerId,
    @NotNull @SupportedCurrency String currency) {

}
