package nc.sgcb.labs.customer.web;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

public record CustomerResponse(
    @NotNull Long id,
    @NotNull String firstName,
    @NotNull String lastName,
    @NotNull LocalDate birthDate,
    @NotNull String birthLocation) {

}
