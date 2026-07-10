package nc.sgcb.labs.account;

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
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.jpa.AccountJpaRepository;
import nc.sgcb.labs.commons.domain.IbanStringMapper;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
  @Autowired(required = false)
  Optional<AccountJpaRepository> accountRepo;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringAccountConverter(accountRepo));
  }

  @RequiredArgsConstructor
  static class StringAccountConverter implements Converter<String, Account> {
    private final Optional<AccountJpaRepository> accountRepo;

    @Override
    public @Nullable Account convert(@Nullable String source) {
      return source == null ? null
          : accountRepo.flatMap(r -> r.findById(IbanStringMapper.mapStringToIban(source)))
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
  }
}
