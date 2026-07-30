package nc.sgcb.labs.account;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.jpa.AccountRepository;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.domain.IbanStringMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;

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
      return iban == null ? null :accountRepo.findById(iban).orElse(null);
    }
  }
}
