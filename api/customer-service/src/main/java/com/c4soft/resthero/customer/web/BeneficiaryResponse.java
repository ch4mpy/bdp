package com.c4soft.resthero.customer.web;

import jakarta.validation.constraints.NotNull;

public record BeneficiaryResponse(@NotNull Long id, @NotNull String label, @NotNull String iban) {
}
