/**
 *
 */
package nc.sgcb.labs.user.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
    @NotNull @Email String email,
    @NotEmpty String username,
    @NotEmpty String firstName,
    @NotEmpty String lastName) {
}
