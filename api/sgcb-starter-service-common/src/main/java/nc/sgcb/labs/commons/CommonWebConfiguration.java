package nc.sgcb.labs.commons;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.domain.IbanStringMapper;

@Configuration
public class CommonWebConfiguration implements WebMvcConfigurer {
  @Autowired
  @Setter
  private IbanStringMapper ibanStringMapper;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringIbanConverter(ibanStringMapper));
  }

  @RequiredArgsConstructor
  static class StringIbanConverter implements Converter<String, Iban> {
    private final IbanStringMapper ibanStringMapper;

    @Override
    public @Nullable Iban convert(@Nullable String source) {
      return ibanStringMapper.map(source);
    }
  }

  static class StringLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public @Nullable LocalDate convert(@Nullable String source) {
      return source == null ? null : LocalDate.parse(source);
    }
  }
}
