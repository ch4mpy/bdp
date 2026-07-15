/**
 *
 */
package nc.sgcb.labs.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
}
