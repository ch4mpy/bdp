/**
 *
 */
package com.c4soft.resthero.customer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import com.c4soft.resthero.commons.security.ServicesRolesAuthoritiesConverter;

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
