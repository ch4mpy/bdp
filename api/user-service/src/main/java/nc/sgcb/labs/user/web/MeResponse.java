/**
 * 
 */
package nc.sgcb.labs.user.web;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record MeResponse(
    @Nullable String sub,
    @Nullable String email,
    @Nullable String username,
    List<String> permissions) {
  public static final MeResponse ANONYMOUS = new MeResponse(null, null, null, List.of());
}