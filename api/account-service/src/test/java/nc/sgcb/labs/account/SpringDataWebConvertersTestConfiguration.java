package nc.sgcb.labs.account;

import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.jpa.AccountJpaRepository;
import nc.sgcb.labs.account.jpa.MoneyTransferJpaRepository;
import nc.sgcb.labs.commons.domain.Iban;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

@TestConfiguration
public class SpringDataWebConvertersTestConfiguration {
  @Autowired(required = false)
  Optional<AccountJpaRepository> accountRepo;

  @Autowired(required = false)
  Optional<MoneyTransferJpaRepository> transferRepo;

  @Bean
  WebMvcConfigurer configurer() {
    return new WebMvcConfigurer() {

      @Override
      public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(
            Iban.class,
            Account.class,
            iban -> accountRepo
                .flatMap(r -> iban == null ? Optional.empty() : r.findById(iban))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));

        registry.addConverter(
            String.class,
            MoneyTransfer.class,
            id -> transferRepo
                .flatMap(r -> id == null ? Optional.empty() : r.findById(Long.valueOf(id)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
      }
    };
  }

}
