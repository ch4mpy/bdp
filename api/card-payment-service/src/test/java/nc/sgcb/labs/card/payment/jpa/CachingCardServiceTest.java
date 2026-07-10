package nc.sgcb.labs.card.payment.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.commons.domain.Iban;

@SpringBootTest
@ActiveProfiles("h2")
class CachingCardServiceTest {

  @MockitoSpyBean
  @SuppressWarnings("null")
  CardJpaRepository cardRepo;

  @Autowired
  CachingCardService cardService;

  Map<String, Card> cardDatabase = new HashMap<>();

  @SuppressWarnings("null")
  String cardNumber;

  @SuppressWarnings("null")
  Iban accountIban;

  @SuppressWarnings("null")
  Card.Ceilings ceilings;

  @BeforeEach
  void setUp() {
    cardNumber = "123456";
    accountIban = Iban.parse("FR76 1111222233334444");
    ceilings = Card.Ceilings.builder().transaction(50000L).rolling30(100000L).build();

    cardRepo.save(Card.builder().number(cardNumber).iban(accountIban).ceilings(ceilings).build());
  }

  @Test
  @DirtiesContext // prevent cache operation conflict between tests
  void givenFindByIdCalledTwiceWithSameNumber_whenSaveCardWithSameNumberAndCallFindByIdAgain_thenCacheUpdatedAndFindByIdCalledOnlyOnceOverall() {
    // cardService.findByNumber called twice, but underlying cardRepo.findById should be called only
    // once.
    var actual = cardService.findByNumber(cardNumber);
    var actual2 = cardService.findByNumber(cardNumber);
    assertEquals(100000L, actual.get().getCeilings().getRolling30());
    assertEquals(100000L, actual2.get().getCeilings().getRolling30());

    // save a new Card instance with the same card number and different ceilings
    // (do not work with a reference to the instance already in the cache)
    var card = cardService.save(Card.builder().iban(accountIban)
        .ceilings(new Card.Ceilings(150000L, 300000L)).number(cardNumber).build());
    assertEquals(300000L, card.getCeilings().getRolling30());

    // retrieve the card from the cache to verify that it was updated when saving the new instance
    var actual3 = cardService.findByNumber(cardNumber);
    assertEquals(300000L, actual3.get().getCeilings().getRolling30());

    // only the 1st call to cardService.findByNumber should delegate to cardRepo.findById
    // (save should @CachePut here, not @CacheEvict)
    verify(cardRepo, times(1)).findById(cardNumber);
  }

  @Test
  @DirtiesContext // prevent cache operation conflict between tests
  void givenFindByIbanCalledTwiceWithSameIban_whenSaveCardWithSameIbanAndCallFindByIbanAgain_thenCacheEvictedAndFindByIbanCalledOnlyTwiceOverall() {
    // cardService.findByNumber called twice, but underlying cardRepo.findById should be called only
    // once.
    var actual = cardService.findByIban(accountIban);
    var actual2 = cardService.findByIban(accountIban);
    assertEquals(100000L, actual.get(0).getCeilings().getRolling30());
    assertEquals(100000L, actual2.get(0).getCeilings().getRolling30());

    // save a new Card instance with the same card number and different ceilings
    // (do not work with a reference to the instance already in the cache)
    var card = cardService.save(Card.builder().iban(accountIban)
        .ceilings(new Card.Ceilings(150000L, 300000L)).number(cardNumber).build());
    assertEquals(300000L, card.getCeilings().getRolling30());

    // retrieve the card from the cache to verify that it was evicted when saving the new instance
    var actual3 = cardService.findByIban(accountIban);
    assertEquals(300000L, actual3.get(0).getCeilings().getRolling30());

    // only the 1st and 3rd calls to cardService.findByNumber should delegate to cardRepo.findById
    // (save should @CacheEvict here, not @CachePut)
    verify(cardRepo, times(2)).findByIban(accountIban);
  }

}
