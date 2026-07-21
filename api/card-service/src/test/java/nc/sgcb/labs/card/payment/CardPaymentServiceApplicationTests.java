package nc.sgcb.labs.card.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import nc.sgcb.labs.account.api.AccountsApi;
import nc.sgcb.labs.account.api.MoneyTransfersApi;
import nc.sgcb.labs.account.model.AccountResponse;
import nc.sgcb.labs.card.payment.jpa.CardPaymentRepository;
import nc.sgcb.labs.card.payment.jpa.CardRepository;
import nc.sgcb.labs.card.payment.web.CardController;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
@Import({MockedOAuth2ClientTestConfiguration.class})
@AutoConfigureMockMvc
class CardPaymentServiceApplicationTests {

  @MockitoBean
  AccountsApi accountsApi;

  @MockitoBean
  MoneyTransfersApi transfersApi;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  CardRepository cardRepo;

  @Autowired
  CardPaymentRepository paymentRepo;

  private static AccountResponse accountOwnedBy(String customerId, String iban, String currency) {
    return new AccountResponse().iban(iban).customerId(customerId).currency(currency).balance(0L);
  }

  @Test
  void contextLoads() {}

  @Test
  @WithMockUser(username = CardFixtures.CUSTOMER_SUBJECT)
  void givenMoneyTransferFails_whenCreateCardPayment_thenPaymentIsPersistedAsNotAccepted()
      throws Exception {
    var card = cardRepo.save(CardFixtures.createCustomersCard(1000L, 5000L));

    when(accountsApi.getAccount(card.getIban().toMachineReadableString()))
        .thenReturn(
            ResponseEntity
                .ok(
                    accountOwnedBy(
                        CardFixtures.CUSTOMER_SUBJECT,
                        card.getIban().toMachineReadableString(),
                        "XPF")));
    when(accountsApi.getAccount(CardFixtures.SOMEONE_IBAN))
        .thenReturn(
            ResponseEntity
                .ok(
                    accountOwnedBy(
                        CardFixtures.SOMEONE_SUBJECT,
                        CardFixtures.SOMEONE_IBAN,
                        "XPF")));
    when(transfersApi.transferMoneyBetweenAccounts(any()))
        .thenThrow(
            HttpClientErrorException
                .create(HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, new byte[0], null));

    mockMvc
        .perform(
            post("https://localhost" + CardController.PAYMENT_LIST_PATH, card.getNumber())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"currency\": \"XPF\",
                      \"amount\": 100,
                      \"cardNumber\": \"%s\",
                      \"destinationIban\": \"%s\"
                    }
                    """.formatted(card.getNumber(), CardFixtures.SOMEONE_IBAN)))
        .andExpect(status().is5xxServerError());

    assertThat(paymentRepo.findAll()).singleElement().satisfies(payment -> {
      assertThat(payment.getCard().getNumber()).isEqualTo(card.getNumber());
      assertThat(payment.getDestinationIban().toMachineReadableString())
          .isEqualTo(CardFixtures.SOMEONE_IBAN);
      assertThat(payment.getAmount().getDigits()).isEqualTo(100L);
      assertThat(payment.isAccepted()).isFalse();
    });
  }
}
