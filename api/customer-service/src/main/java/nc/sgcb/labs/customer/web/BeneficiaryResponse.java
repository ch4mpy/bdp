package nc.sgcb.labs.customer.web;

import jakarta.validation.constraints.NotNull;

public record BeneficiaryResponse(@NotNull Long id, @NotNull String label, @NotNull String iban) {
}
