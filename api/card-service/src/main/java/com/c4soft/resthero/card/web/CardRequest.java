package com.c4soft.resthero.card.web;

import com.c4soft.resthero.commons.validation.IbanString;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CardRequest(
    @NotNull @IbanString String iban,
    @NotNull @Min(1) Integer transactionCeiling,
    @NotNull @Min(1) Integer rolling30Ceiling) {
}
