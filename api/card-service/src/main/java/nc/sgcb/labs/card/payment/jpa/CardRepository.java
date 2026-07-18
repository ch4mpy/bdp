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
import nc.sgcb.labs.commons.domain.Iban;

@Service
@CacheConfig(cacheNames = {CardRepository.CARD_BY_NUMBER_CACHE, CardRepository.CARD_BY_IBAN_CACHE})
@RequiredArgsConstructor
public class CardRepository {
  static final String CARD_BY_NUMBER_CACHE = "cardByNumber";
  static final String CARD_BY_IBAN_CACHE = "cardByIban";

  private final JpaCardRepository cardRepo;

  @Cacheable(cacheNames = CARD_BY_NUMBER_CACHE)
  public Optional<Card> findByNumber(String number) {
    return cardRepo.findById(number);
  }

  @Caching(put = @CachePut(cacheNames = CARD_BY_NUMBER_CACHE, key = "#card.number"),
      evict = @CacheEvict(cacheNames = CardRepository.CARD_BY_IBAN_CACHE, key = "#card.iban"))
  public Card save(Card card) {
    return cardRepo.save(card);
  }

  @Cacheable(cacheNames = CARD_BY_IBAN_CACHE)
  public List<Card> findByIban(Iban iban) {
    return cardRepo.findByIban(iban);
  }



}
