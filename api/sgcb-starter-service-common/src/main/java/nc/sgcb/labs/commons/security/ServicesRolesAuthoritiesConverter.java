package nc.sgcb.labs.commons.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.c4_soft.springaddons.security.oidc.starter.ClaimSetAuthoritiesConverter;

/**
 * Extracts client roles for all clients suffixed with "-service" (customer-service,
 * account-service, card-service, user-service, ...).
 * 
 * Extracted roles are prefixed with the service name (i.e. the "read_any" role for the
 * "account-service" => "account.read_any" authority).
 * 
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
public class ServicesRolesAuthoritiesConverter implements ClaimSetAuthoritiesConverter {

  @SuppressWarnings({"unchecked"})
  @Override
  public Collection<? extends GrantedAuthority> convert(Map<String, Object> source) {
    final var resourceAccess =
        (Map<String, Object>) source.getOrDefault("resource_access", Map.of());
    return resourceAccess
        .entrySet()
        .stream()
        .filter(e -> e.getKey().endsWith("-service"))
        .flatMap(e -> {
          var roles =
              (List<String>) ((Map<String, Object>) e.getValue()).getOrDefault("roles", List.of());
          return roles.stream().map(r -> "%s.%s".formatted(e.getKey().replace("-service", ""), r));
        })
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

}
