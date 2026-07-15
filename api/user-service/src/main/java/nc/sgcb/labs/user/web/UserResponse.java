/**
 *
 */
package nc.sgcb.labs.user.web;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record UserResponse(
    @Nullable String sub,
    @Nullable String email,
    @Nullable String username,
    @Nullable String firstName,
    @Nullable String lastName,
    List<String> roles) {
  public static final UserResponse ANONYMOUS =
      new UserResponse(null, null, null, null, null, List.of());
}
