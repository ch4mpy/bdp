package nc.sgcb.labs.customer.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BeneficiaryRequest(@NotEmpty String iban, @NotEmpty @Size(max = 256) String label) {
}
