package com.c4soft.resthero.account;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.c4soft.resthero.account.domain.Account;
import com.c4soft.resthero.account.jpa.AccountRepository;
import com.c4soft.resthero.commons.domain.IbanStringMapper;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {
  private final IbanStringMapper ibanStringMapper;
  private final AccountRepository accountRepo;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringAccountConverter(ibanStringMapper, accountRepo));
  }

  @RequiredArgsConstructor
  static class StringAccountConverter implements Converter<String, Account> {
    private final IbanStringMapper ibanStringMapper;
    private final AccountRepository accountRepo;

    @Override
    public @Nullable Account convert(@Nullable String source) {
      final var iban = ibanStringMapper.map(source);
      return iban == null ? null : accountRepo.findByIban(iban).orElse(null);
    }
  }
}
