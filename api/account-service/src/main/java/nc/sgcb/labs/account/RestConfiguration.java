package nc.sgcb.labs.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestClient;
import com.c4_soft.springaddons.rest.RestClientHttpExchangeProxyFactoryBean;
import nc.sgcb.labs.customer.api.CustomersApi;

@Configuration
public class RestConfiguration {

  @Bean
  CustomersApi usersApi(RestClient customerServiceClient) throws Exception {
    return new RestClientHttpExchangeProxyFactoryBean<>(CustomersApi.class, customerServiceClient)
        .getObject();
  }

  @Bean
  OAuth2AuthorizedClientManager oauth2AuthorizedClientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService authorizedClientService) {
    return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
        clientRegistrationRepository,
        authorizedClientService);
  }

}
