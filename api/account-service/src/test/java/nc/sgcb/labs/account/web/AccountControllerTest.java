package nc.sgcb.labs.account.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import nc.sgcb.labs.account.AccountFixtures;
import nc.sgcb.labs.account.SecurityConfig;
import nc.sgcb.labs.account.SpringDataWebConvertersTestConfiguration;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.jpa.AccountRepository;
import nc.sgcb.labs.account.jpa.MoneyTransferRepository;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.customer.api.CustomersApi;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AccountController.class, properties = {})
@Import({AccountMapperImpl.class, MoneyTransferMapperImpl.class,
    SpringDataWebConvertersTestConfiguration.class, SecurityConfig.class})
@AutoConfigureAddonsWebmvcResourceServerSecurity
class AccountControllerTest {

  @MockitoBean
  AccountRepository accountRepo;

  @MockitoBean
  MoneyTransferRepository transferRepo;

  @MockitoBean
  CustomersApi customersApi;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper json;

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenListAccounts_thenUnauthorized() throws Exception {
    mockMvc
        .perform(
            get("https://localhost" + AccountController.BASE_PATH).queryParam("customerId", "1234"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsAdvisor_whenListAccountsForKnownCustomerId_thenMatchingAccountsReturned()
      throws Exception {
    List<Account> accountList = List
        .of(
            AccountFixtures.createCustomersXpfAccount(100000L),
            AccountFixtures.createCustomersEurAccount(200000L));
    when(accountRepo.findByCustomerId(AccountFixtures.CUSTOMER_SUBJECT)).thenReturn(accountList);

    List<AccountResponse> actual = json
        .readValue(
            mockMvc
                .perform(
                    get("https://localhost" + AccountController.BASE_PATH)
                        .queryParam("customerId", AccountFixtures.CUSTOMER_SUBJECT))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            new TypeReference<>() {});

    assertThat(actual).hasSize(accountList.size());
    assertTrue(
        actual.stream().allMatch(dto -> AccountFixtures.CUSTOMER_SUBJECT.equals(dto.customerId())));
  }

  @Test
  @WithJwt("customer.json")
  void givenUserIsACustomer_whenListAccountsForHimself_thenMatchingAccountsReturned()
      throws Exception {
    List<Account> accountList = List
        .of(
            AccountFixtures.createCustomersXpfAccount(100000L),
            AccountFixtures.createCustomersEurAccount(200000L));
    when(accountRepo.findByCustomerId(AccountFixtures.CUSTOMER_SUBJECT)).thenReturn(accountList);

    List<AccountResponse> actual = json
        .readValue(
            mockMvc
                .perform(
                    get("https://localhost" + AccountController.BASE_PATH)
                        .queryParam("customerId", AccountFixtures.CUSTOMER_SUBJECT))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            new TypeReference<>() {});

    assertThat(actual).hasSize(accountList.size());
    assertTrue(
        actual.stream().allMatch(dto -> AccountFixtures.CUSTOMER_SUBJECT.equals(dto.customerId())));
  }

  @Test
  @WithJwt("customer.json")
  void givenUserIsACustomer_whenListSomeoneElseAccounts_thenForbidden() throws Exception {
    List<Account> accountList = List.of(AccountFixtures.createSomeonesXpfAccount(200000L));
    when(accountRepo.findByCustomerId(AccountFixtures.SOMEONE_SUBJECT)).thenReturn(accountList);

    mockMvc
        .perform(
            get("https://localhost" + AccountController.BASE_PATH)
                .queryParam("customerId", AccountFixtures.SOMEONE_SUBJECT))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithJwt("advisor.json")
  void whenListAccountsWithoutACustomerId_thenBadRequest() throws Exception {
    mockMvc
        .perform(get("https://localhost" + AccountController.BASE_PATH))
        .andExpect(status().is4xxClientError());
    mockMvc
        .perform(
            get("https://localhost" + AccountController.BASE_PATH).queryParam("customerId", ""))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsAdvisor_whenGetAccountWithAKnownIban_thenOk() throws Exception {
    var account = AccountFixtures.createCustomersXpfAccount(100000L);
    when(accountRepo.findById(account.getIban())).thenReturn(Optional.of(account));

    var actual = json
        .readValue(
            mockMvc
                .perform(
                    get(
                        "https://localhost" + AccountController.ACCOUNT_PATH,
                        account.getIban().toMachineReadableString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AccountResponse.class);

    assertThat(actual.customerId()).isEqualTo(AccountFixtures.CUSTOMER_SUBJECT);
  }

  @Test
  @WithJwt("customer.json")
  void givenUserIsCustomer_whenGetAccountWithOneOfHisAccountsIban_thenOk() throws Exception {
    var account = AccountFixtures.createCustomersXpfAccount(100000L);
    when(accountRepo.findById(account.getIban())).thenReturn(Optional.of(account));

    var actual = json
        .readValue(
            mockMvc
                .perform(
                    get(
                        "https://localhost" + AccountController.ACCOUNT_PATH,
                        account.getIban().toMachineReadableString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AccountResponse.class);

    assertThat(Iban.of(actual.iban())).isEqualTo(account.getIban());
  }

  @Test
  @WithJwt("customer.json")
  void givenUserIsCustomer_whenGetAccountWithSomeoneElsesAccountsIban_thenForbidden()
      throws Exception {
    var account = AccountFixtures.createSomeonesXpfAccount(100000L);
    when(accountRepo.findById(account.getIban())).thenReturn(Optional.of(account));

    mockMvc
        .perform(
            get(
                "https://localhost" + AccountController.ACCOUNT_PATH,
                account.getIban().toMachineReadableString()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsAdvisor_whenGetAccountWithAnUnknownIban_thenNotFound() throws Exception {
    when(accountRepo.findById(any(Iban.class))).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get(
                "https://localhost" + AccountController.ACCOUNT_PATH,
                AccountFixtures
                    .createCustomersXpfAccount(100000L)
                    .getIban()
                    .toMachineReadableString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenCreateAccount_thenUnauthorized() throws Exception {
    var dto = new AccountCreationRequest(
        AccountFixtures.createCustomersXpfAccount(100000L).getIban().toMachineReadableString(),
        AccountFixtures.CUSTOMER_SUBJECT,
        "XPF");

    mockMvc
        .perform(
            post("https://localhost" + AccountController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserHasAccountCreateAuthority_whenCreateAccountWithValidBody_thenCreated()
      throws Exception {
    var iban = AccountFixtures.createCustomersXpfAccount(100000L).getIban();
    var dto = new AccountCreationRequest(
        iban.toMachineReadableString(),
        AccountFixtures.CUSTOMER_SUBJECT,
        "XPF");

    when(accountRepo.existsById(any(Iban.class))).thenReturn(false);
    when(customersApi.getCustomer(AccountFixtures.CUSTOMER_SUBJECT))
        .thenReturn(org.springframework.http.ResponseEntity.ok(null));
    when(accountRepo.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

    var mvcResult = mockMvc
        .perform(
            post("https://localhost" + AccountController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andReturn();

    var location = mvcResult.getResponse().getHeader("Location");
    assertThat(location).isNotNull();
  }

  @Test
  @WithJwt("customer.json")
  void givenUserDoesNotHaveAccountCreateAuthority_whenCreateAccount_thenForbidden()
      throws Exception {
    var dto = new AccountCreationRequest(
        AccountFixtures.createCustomersXpfAccount(100000L).getIban().toMachineReadableString(),
        AccountFixtures.CUSTOMER_SUBJECT,
        "XPF");

    mockMvc
        .perform(
            post("https://localhost" + AccountController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsHasAccountCreateAuthority_whenCreateAccountWithInvalidPayload_thenBadRequest()
      throws Exception {
    // missing customerId -> pass null
    var dtoMissingCustomer = new AccountCreationRequest(
        AccountFixtures.createCustomersXpfAccount(100000L).getIban().toMachineReadableString(),
        null,
        "XPF");

    mockMvc
        .perform(
            post("https://localhost" + AccountController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dtoMissingCustomer)))
        .andExpect(status().is4xxClientError());

    // invalid currency
    var dtoInvalidCurrency = new AccountCreationRequest(
        AccountFixtures.createCustomersXpfAccount(100000L).getIban().toMachineReadableString(),
        AccountFixtures.CUSTOMER_SUBJECT,
        "XX");

    mockMvc
        .perform(
            post("https://localhost" + AccountController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dtoInvalidCurrency)))
        .andExpect(status().is4xxClientError());

    // invalid iban format
    var dtoInvalidIban =
        new AccountCreationRequest("not-an-iban", AccountFixtures.CUSTOMER_SUBJECT, "XPF");

    mockMvc
        .perform(
            post("https://localhost" + AccountController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dtoInvalidIban)))
        .andExpect(status().is4xxClientError());

    // conflict when account already exists
    var existingIban = AccountFixtures.createCustomersXpfAccount(100000L).getIban();
    var dtoConflict = new AccountCreationRequest(
        existingIban.toMachineReadableString(),
        AccountFixtures.CUSTOMER_SUBJECT,
        "XPF");
    when(accountRepo.existsById(any(Iban.class))).thenReturn(true);

    mockMvc
        .perform(
            post("https://localhost" + AccountController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dtoConflict)))
        .andExpect(status().isConflict());
  }

}
