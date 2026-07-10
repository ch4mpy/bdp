package nc.sgcb.labs.card.payment.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.card.payment.domain.CardService;
import nc.sgcb.labs.commons.domain.Iban;

@Service
@CacheConfig(
    cacheNames = {CachingCardService.CARD_BY_NUMBER_CACHE, CachingCardService.CARD_BY_IBAN_CACHE})
@RequiredArgsConstructor
public class CachingCardService implements CardService {
  static final String CARD_BY_NUMBER_CACHE = "cardByNumber";
  static final String CARD_BY_IBAN_CACHE = "cardByIban";

  private final CardJpaRepository cardRepo;

  @Override
  @Cacheable(cacheNames = CARD_BY_NUMBER_CACHE)
  public Optional<Card> findByNumber(String number) {
    return cardRepo.findById(number);
  }

  @Override
  @Caching(put = @CachePut(cacheNames = CARD_BY_NUMBER_CACHE, key = "#card.number"),
      evict = @CacheEvict(cacheNames = CachingCardService.CARD_BY_IBAN_CACHE, key = "#card.iban"))
  public Card save(Card card) {
    return cardRepo.save(card);
  }

  @Override
  @Cacheable(cacheNames = CARD_BY_IBAN_CACHE)
  public List<Card> findByIban(Iban iban) {
    return cardRepo.findByIban(iban);
  }



}
