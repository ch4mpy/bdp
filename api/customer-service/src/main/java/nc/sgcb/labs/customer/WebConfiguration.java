package nc.sgcb.labs.customer;

import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.customer.domain.Customer;
import nc.sgcb.labs.customer.keycloak.CustomerRepository;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
  @Autowired(required = false)
  Optional<CustomerRepository> customerRepo;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringCustomerConverter(customerRepo));
  }

  @RequiredArgsConstructor
  static class StringCustomerConverter implements Converter<String, Customer> {
    private final Optional<CustomerRepository> customerRepo;

    @Override
    public @Nullable Customer convert(@Nullable String source) {
      return source == null ? null
          : customerRepo
              .flatMap(r -> r.findById(source))
              .orElseThrow(
                  () -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND,
                      "Account %s is not known by the account-service".formatted(source)));
    }
  }
}
