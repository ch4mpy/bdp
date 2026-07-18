package nc.sgcb.labs.card.payment;

import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@TestConfiguration
@Import({OAuth2ClientProperties.class})
public class MockedOAuth2ClientTestConfiguration {
  @Bean
  ClientRegistrationRepository clientRegistrationRepository(
      OAuth2ClientProperties oauth2ClientProperties) {

    return new InMemoryClientRegistrationRepository(
        oauth2ClientProperties.getRegistration().entrySet().stream().map(entry -> {
          var registrationId = entry.getKey();
          var registration = entry.getValue();
          return ClientRegistration
              .withRegistrationId(registrationId)
              .authorizationGrantType(
                  new AuthorizationGrantType(registration.getAuthorizationGrantType()))
              .clientId(registration.getClientId())
              .clientSecret(registration.getClientSecret())
              .tokenUri("https://test.idp/token")
              .authorizationUri("https://test.idp/auth")
              .redirectUri("https://client/oauth2/%s/code".formatted(registrationId))
              .build();
        }).toList());
  }
}
