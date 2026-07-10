package nc.sgcb.labs.account.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithMockAuthentication;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import nc.sgcb.labs.account.SpringDataWebConvertersTestConfiguration;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.jpa.AccountJpaRepository;
import nc.sgcb.labs.account.jpa.MoneyTransferJpaRepository;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(properties = {"logging.level.org.springframework=DEBUG"})
@Import({AccountMapperImpl.class, MoneyTransferMapperImpl.class,
    SpringDataWebConvertersTestConfiguration.class})
@AutoConfigureAddonsWebmvcResourceServerSecurity
class AccountControllerTest {

  @MockitoBean
  AccountJpaRepository accountRepo;

  @MockitoBean
  MoneyTransferJpaRepository transferRepo;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper json;

  @Test
  @WithMockAuthentication
  void givenKnownCustomerId_whenListAccounts_thenMatchingAccountsReturned() throws Exception {
    final var iban = Iban.parse("FR761111222233334444");
    final var customerId = Long.valueOf(1234L);
    final var balance = Amount.builder().currencyIso3("XPF").digits(123456L).build();
    List<Account> accountList =
        List.of(Account.builder().customerId(customerId).iban(iban).balance(balance).build());
    when(accountRepo.findByCustomerId(customerId)).thenReturn(accountList);

    List<AccountResponse> actual = json.readValue(mockMvc
        .perform(get("https://localhost" + AccountController.BASE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .queryParam("customerId", customerId.toString()))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
        new TypeReference<>() {});

    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).customerId()).isEqualTo(customerId);
  }

  @Test
  @WithMockAuthentication
  void givenNoCustomerId_whenListAccounts_thenBadRequest() throws Exception {
    mockMvc.perform(get("https://localhost" + AccountController.BASE_PATH)
        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockAuthentication
  void givenKnownIban_whenGetAccount_thenOk() throws Exception {
    final var iban = Iban.parse("FR761111222233334444");
    final var customerId = Long.valueOf(1234L);
    final var balance = Amount.builder().currencyIso3("XPF").digits(123456L).build();
    Account account = Account.builder().customerId(customerId).iban(iban).balance(balance).build();
    when(accountRepo.findById(iban)).thenReturn(Optional.of(account));

    var actual = json.readValue(
        mockMvc
            .perform(get("https://localhost" + AccountController.ACCOUNT_PATH,
                iban.toMachineReadableString()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
        AccountResponse.class);

    assertThat(actual.customerId()).isEqualTo(customerId);
  }

  @Test
  @WithMockAuthentication
  void givenUnknownIban_whenGetAccount_thenNotFound() throws Exception {
    final var iban = Iban.parse("FR761111222233334444");
    when(accountRepo.findById(iban)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("https://localhost" + AccountController.ACCOUNT_PATH,
            iban.toMachineReadableString()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

}
