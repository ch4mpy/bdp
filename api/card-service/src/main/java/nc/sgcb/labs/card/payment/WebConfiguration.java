package nc.sgcb.labs.card.payment;

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
import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.card.payment.jpa.CardRepository;

/**
 * Registers the {@link Converter} required to resolve a {@link Card} from its number as used in
 * {@code @PathVariable} of {@code CardController}.
 *
 * @author Jerome Wacongne ch4mp&#64;c4-soft.com
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
  @Autowired(required = false)
  Optional<CardRepository> cardRepo;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringCardConverter(cardRepo));
  }

  @RequiredArgsConstructor
  static class StringCardConverter implements Converter<String, Card> {
    private final Optional<CardRepository> cardRepo;

    @Override
    public @Nullable Card convert(@Nullable String source) {
      return source == null ? null
          : cardRepo
              .flatMap(r -> r.findByNumber(source))
              .orElseThrow(
                  () -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND,
                      "Card %s is not known by the card-service".formatted(source)));
    }
  }
}
