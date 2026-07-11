package nc.sgcb.labs.commons;

import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.domain.IbanStringMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringIbanConverter());
  }

  static class StringIbanConverter implements Converter<String, Iban> {

    @Override
    public @Nullable Iban convert(@Nullable String source) {
      return source == null ? null : IbanStringMapper.mapStringToIban(source);
    }
  }
}
