package nc.sgcb.labs.customer.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nc.sgcb.labs.commons.validation.IbanString;

public record BeneficiaryRequest(
    @NotNull @IbanString String iban,
    @NotEmpty @Size(max = 256) String label) {
}
