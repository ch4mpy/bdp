package com.c4soft.resthero.customer.web;

import com.c4soft.resthero.commons.validation.IbanString;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BeneficiaryRequest(
    @NotNull @IbanString String iban,
    @NotEmpty @Size(max = 256) String label) {
}
