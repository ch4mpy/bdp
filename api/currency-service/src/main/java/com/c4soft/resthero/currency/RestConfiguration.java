package com.c4soft.resthero.currency;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import com.c4_soft.springaddons.rest.RestClientHttpExchangeProxyFactoryBean;
import dev.frankfurter.api.RatesApi;

@Configuration
public class RestConfiguration {

  // LAB:2.2:REMOVE:START
  @Bean
  RatesApi customersApi(RestClient frankfurterClient) throws Exception {
    return new RestClientHttpExchangeProxyFactoryBean<>(RatesApi.class, frankfurterClient)
        .getObject();
  }
  // LAB:2.2:REMOVE:END

}
