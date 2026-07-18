package nc.sgcb.labs.customer;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import nc.sgcb.labs.customer.domain.Customer;
import nc.sgcb.labs.customer.jpa.CustomerRepository;

@TestConfiguration
public class SpringDataWebConvertersTestConfiguration {
  @Autowired(required = false)
  Optional<CustomerRepository> customerRepo;

  @Bean
  WebMvcConfigurer configurer() {
    return new WebMvcConfigurer() {

      @Override
      public void addFormatters(FormatterRegistry registry) {
        registry
            .addConverter(
                String.class,
                Customer.class,
                id -> customerRepo
                    .flatMap(r -> id == null ? Optional.empty() : r.findById(id))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
      }
    };
  }

}
