/**
 *
 */
package nc.sgcb.labs.account;

import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.account.jpa.AccountRepository;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.security.ServicesRolesAuthoritiesConverter;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  public static final String ACCESS_CONTROL_BEAN_NAME = "ac";

  @Bean
  ServicesRolesAuthoritiesConverter authoritiesConverter() {
    return new ServicesRolesAuthoritiesConverter();
  }

  @Component(ACCESS_CONTROL_BEAN_NAME)
  @RequiredArgsConstructor
  public static class AccessControlHelper {
    private final AccountRepository accountRepo;

    public boolean isAccountOwner(@Nullable String customerId, @Nullable String ibanString) {
      return ibanString == null
          ? false
          : accountRepo
              .findByIban(Iban.of(ibanString))
              .map(account -> Objects.equals(customerId, account.getCustomerId()))
              .orElse(false);
    }

    public boolean ownsAccount(@Nullable String ibanString) {
      var auth = SecurityContextHolder.getContext().getAuthentication();
      return auth != null && isAccountOwner(auth.getName(), ibanString);
    }
  }
}
