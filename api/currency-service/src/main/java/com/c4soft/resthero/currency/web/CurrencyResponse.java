package com.c4soft.resthero.currency.web;

import jakarta.validation.constraints.NotNull;

public record CurrencyResponse(@NotNull String iso3, short decimals) {

}
