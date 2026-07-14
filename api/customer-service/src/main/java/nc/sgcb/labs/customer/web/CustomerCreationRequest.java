package nc.sgcb.labs.customer.web;

import java.time.LocalDate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CustomerCreationRequest(
    @NotEmpty String firstName,
    @NotEmpty String lastName,
    @NotNull LocalDate birthDate,
    @NotEmpty String birthLocation) {

}
