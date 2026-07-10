package nc.sgcb.labs.card.payment.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.commons.domain.Iban;

@SpringBootTest
@ActiveProfiles("h2")
class CachingCardServiceTest {

  @Autowired
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
    ceilings = new Card.Ceilings(50000L, 100000L);

    cardRepo.save(Card.builder().number(cardNumber).iban(accountIban).ceilings(ceilings).build());
  }

  @Test
  void test() {
    var actual = cardService.findByNumber(cardNumber);
    var actual2 = cardService.findByNumber(cardNumber);
    assertEquals(100000L, actual.get().getCeilings().getRolling30());
    assertEquals(100000L, actual2.get().getCeilings().getRolling30());

    var card = cardService.save(Card.builder().iban(accountIban)
        .ceilings(new Card.Ceilings(150000L, 300000L)).number(cardNumber).build());
    assertEquals(300000L, card.getCeilings().getRolling30());

    var actual3 = cardService.findByNumber(cardNumber);
    assertEquals(300000L, actual3.get().getCeilings().getRolling30());
  }

}
