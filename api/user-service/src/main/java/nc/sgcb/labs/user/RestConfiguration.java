/**
 *
 */
package nc.sgcb.labs.user;

import org.keycloak.admin.api.GroupsApi;
import org.keycloak.admin.api.UsersApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestClient;
import com.c4_soft.springaddons.rest.RestClientHttpExchangeProxyFactoryBean;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Configuration
@Slf4j
public class RestConfiguration {

  @Bean
  UsersApi usersApi(RestClient keycloakAdminApi) throws Exception {
    return new RestClientHttpExchangeProxyFactoryBean<>(UsersApi.class, keycloakAdminApi)
        .getObject();
  }

  @Bean
  GroupsApi groupsApi(RestClient keycloakAdminApi) throws Exception {
    log.warn(
        "Instantiate %s.%s".formatted(GroupsApi.class.getPackageName(), GroupsApi.class.getName()));
    return new RestClientHttpExchangeProxyFactoryBean<>(GroupsApi.class, keycloakAdminApi)
        .getObject();
  }

  @Bean
  OAuth2AuthorizedClientManager oauth2AuthorizedClientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService authorizedClientService) {
    return new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
        authorizedClientService);
  }

}
