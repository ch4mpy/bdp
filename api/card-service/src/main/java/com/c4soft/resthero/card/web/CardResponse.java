package com.c4soft.resthero.card.web;

import jakarta.validation.constraints.NotNull;

public record CardResponse(
    @NotNull String number,
    @NotNull String iban,
    @NotNull Integer transactionCeiling,
    @NotNull Integer rolling30Ceiling,
    boolean isActive) {

}
