package com.c4soft.resthero.account;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.c4soft.resthero.account.domain.Account;
import com.c4soft.resthero.account.domain.MoneyTransfer;
import com.c4soft.resthero.account.jpa.AccountRepository;
import com.c4soft.resthero.account.jpa.MoneyTransferRepository;
import com.c4soft.resthero.commons.domain.Iban;

@TestConfiguration
public class SpringDataWebConvertersTestConfiguration {
  @Autowired(required = false)
  Optional<AccountRepository> accountRepo;

  @Autowired(required = false)
  Optional<MoneyTransferRepository> transferRepo;

  @Bean
  WebMvcConfigurer configurer() {
    return new WebMvcConfigurer() {

      @Override
      public void addFormatters(FormatterRegistry registry) {
        registry
            .addConverter(
                String.class,
                Account.class,
                iban -> accountRepo
                    .flatMap(r -> iban == null ? Optional.empty() : r.findByIban(Iban.of(iban)))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));

        registry
            .addConverter(
                String.class,
                MoneyTransfer.class,
                id -> transferRepo
                    .flatMap(r -> id == null ? Optional.empty() : r.findById(Long.valueOf(id)))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
      }
    };
  }

}
