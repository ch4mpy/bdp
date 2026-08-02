package com.c4soft.resthero.card.web;

import java.time.Instant;
import jakarta.validation.constraints.NotNull;

public record CardPaymentResponse(
    @NotNull Long id,
    @NotNull Instant timestamp,
    @NotNull String currency,
    @NotNull Integer amount,
    @NotNull String cardNumber,
    @NotNull String destinationIban,
    boolean isAccepted) {
}
