package nc.sgcb.labs.customer.web;

import jakarta.validation.constraints.NotNull;

public record CustomerResponse(
    @NotNull String id,
    @NotNull String firstName,
    @NotNull String lastName,
    @NotNull String email) {

}
