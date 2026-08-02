package com.c4soft.resthero.card.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CardCeilingsRequest(
    @NotNull @Min(1) Integer transactionCeiling,
    @NotNull @Min(1) Integer rolling30Ceiling) {

}
