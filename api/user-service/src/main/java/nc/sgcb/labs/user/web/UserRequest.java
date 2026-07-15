/**
 *
 */
package nc.sgcb.labs.user.web;

import org.jspecify.annotations.Nullable;

public record UserRequest(
    @Nullable String email,
    @Nullable String username,
    @Nullable String firstName,
    @Nullable String lastName) {
}
