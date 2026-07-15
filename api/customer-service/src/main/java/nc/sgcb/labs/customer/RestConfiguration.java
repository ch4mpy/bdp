package nc.sgcb.labs.customer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestClient;
import com.c4_soft.springaddons.rest.RestClientHttpExchangeProxyFactoryBean;
import nc.sgcb.labs.user.api.UsersApi;

@Configuration
public class RestConfiguration {

  @Bean
  UsersApi usersApi(RestClient userServiceClient) throws Exception {
    return new RestClientHttpExchangeProxyFactoryBean<>(UsersApi.class, userServiceClient)
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
