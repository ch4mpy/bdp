package com.c4soft.resthero.account.web;

import jakarta.validation.constraints.NotNull;

public record AccountResponse(
    @NotNull String iban,
    @NotNull String customerId,
    @NotNull String currency,
    @NotNull Integer balance) {

}
