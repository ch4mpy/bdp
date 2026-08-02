package com.c4soft.resthero.card.web;

import com.c4soft.resthero.commons.validation.IbanString;
import com.c4soft.resthero.commons.validation.SupportedCurrency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CardPaymentCreationRequest(
    @NotNull @SupportedCurrency String currency,
    @NotNull @Min(1) Integer amount,
    @Size(min = 1, max = 36) String cardNumber,
    @NotNull @IbanString String destinationIban) {
}
