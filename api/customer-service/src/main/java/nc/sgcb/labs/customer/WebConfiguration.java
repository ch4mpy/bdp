package nc.sgcb.labs.customer;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.customer.domain.Customer;
import nc.sgcb.labs.customer.keycloak.CustomerRepository;

@Configuration
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {
  private final CustomerRepository customerRepo;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringCustomerConverter(customerRepo));
  }

  @RequiredArgsConstructor
  static class StringCustomerConverter implements Converter<String, Customer> {
    private final CustomerRepository repo;

    @Override
    public @Nullable Customer convert(@Nullable String source) {
      return source == null ? null : repo.findById(source).orElse(null);
    }
  }
}
