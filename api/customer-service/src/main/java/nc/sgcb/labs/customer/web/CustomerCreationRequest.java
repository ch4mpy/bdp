package nc.sgcb.labs.customer.web;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerCreationRequest(
    @NotEmpty String firstName,
    @NotEmpty String lastName,
    @NotNull LocalDate birthDate,
    @Size(min = 1, max = 128) String birthLocation,
    @NotNull @Email String email) {

}
