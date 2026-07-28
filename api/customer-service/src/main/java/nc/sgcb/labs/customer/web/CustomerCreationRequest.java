package nc.sgcb.labs.customer.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CustomerCreationRequest(
    @NotEmpty String firstName,
    @NotEmpty String lastName,
    @NotNull @Email String email) {

}
