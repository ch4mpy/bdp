package com.c4soft.resthero.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestClient;
import com.c4_soft.springaddons.rest.RestClientHttpExchangeProxyFactoryBean;
import com.c4soft.resthero.api.CurrenciesApi;
import com.c4soft.resthero.api.CustomersApi;

// LAB:2.4:REMOVE:START
@Configuration
// LAB:2.4:REMOVE:END
public class RestConfiguration {

  @SuppressWarnings("null")
  @Bean
  CustomersApi customersApi(RestClient customerServiceClient) throws Exception {
    return new RestClientHttpExchangeProxyFactoryBean<>(CustomersApi.class, customerServiceClient)
        .getObject();
  }

  @SuppressWarnings("null")
  @Bean
  CurrenciesApi currenciesApi(RestClient currenciesServiceClient) throws Exception {
    return new RestClientHttpExchangeProxyFactoryBean<>(
        CurrenciesApi.class,
        currenciesServiceClient).getObject();
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
