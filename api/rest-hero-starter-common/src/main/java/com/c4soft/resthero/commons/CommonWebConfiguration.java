package com.c4soft.resthero.commons;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.c4soft.resthero.commons.domain.Iban;
import com.c4soft.resthero.commons.domain.IbanStringMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Configuration
@RequiredArgsConstructor
public class CommonWebConfiguration implements WebMvcConfigurer {
  private final IbanStringMapper ibanStringMapper;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    // LAB:4.2:TODO:START enregistrer le convertisseur Iban partagé
    registry.addConverter(new StringIbanConverter(ibanStringMapper));
    // LAB:4.2:TODO:END
  }

  @RequiredArgsConstructor
  static class StringIbanConverter implements Converter<String, Iban> {
    private final IbanStringMapper ibanStringMapper;

    @Override
    public @Nullable Iban convert(@Nullable String source) {
      return ibanStringMapper.map(source);
    }
  }
}
