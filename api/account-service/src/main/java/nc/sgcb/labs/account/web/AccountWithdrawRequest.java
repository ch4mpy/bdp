package nc.sgcb.labs.account.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import nc.sgcb.labs.commons.validation.Iban;

public record AccountWithdrawRequest(
    @NotNull @Iban String toIban,
    @Min(1) Long amount,
    @Pattern(regexp = "\\w{3}") String currency,
    @NotEmpty String label) {

}
