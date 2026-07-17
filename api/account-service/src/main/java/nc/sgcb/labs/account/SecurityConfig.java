/**
 *
 */
package nc.sgcb.labs.account;

import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.account.jpa.AccountRepository;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.security.ServicesRolesAuthoritiesConverter;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  ServicesRolesAuthoritiesConverter authoritiesConverter() {
    return new ServicesRolesAuthoritiesConverter();
  }

  @Component
  @RequiredArgsConstructor
  public static class TransferAccessControl {
    private final AccountRepository accountRepo;

    public boolean isAccountOwner(String customerId, String ibanString) {
      return accountRepo
          .findById(Iban.parse(ibanString))
          .map(account -> Objects.equals(customerId, account.getCustomerId()))
          .orElse(false);
    }
  }
}
