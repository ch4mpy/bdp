/**
 *
 */
package nc.sgcb.labs.user.web;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record MeResponse(
    @Nullable String sub,
    @Nullable String email,
    @Nullable String username,
    List<String> permissions) {
  public static final MeResponse ANONYMOUS = new MeResponse(null, null, null, List.of());
}
