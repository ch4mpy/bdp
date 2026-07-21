package nc.sgcb.labs.account.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import nc.sgcb.labs.account.AccountFixtures;
import nc.sgcb.labs.account.MoneyTransferFixtures;
import nc.sgcb.labs.account.SecurityConfig;
import nc.sgcb.labs.account.SpringDataWebConvertersTestConfiguration;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.jpa.AccountRepository;
import nc.sgcb.labs.account.jpa.MoneyTransferRepository;
import nc.sgcb.labs.commons.domain.Iban;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = MoneyTransferController.class,
    properties = {"logging.level.org.springframework=DEBUG"})
@Import({MoneyTransferMapperImpl.class, SpringDataWebConvertersTestConfiguration.class,
    SecurityConfig.class})
@AutoConfigureAddonsWebmvcResourceServerSecurity
class MoneyTransferControllerTest {

  @MockitoBean
  AccountRepository accountRepo;

  @MockitoBean
  MoneyTransferRepository transferRepo;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper json;

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenListTransfers_thenUnauthorized() throws Exception {
    mockMvc
        .perform(get("https://localhost" + MoneyTransferController.BASE_PATH))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsAdvisor_whenListTransfers_thenOk() throws Exception {
    Account source = AccountFixtures.createCustomersXpfAccount(100000L);
    Account destination = AccountFixtures.createSomeonesXpfAccount(200000L);

    MoneyTransfer transfer = MoneyTransferFixtures.createMoneyTransfer(source, destination, 1000L);

    when(transferRepo.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(transfer)));

    var mvcResult = mockMvc
        .perform(
            get("https://localhost" + MoneyTransferController.BASE_PATH)
                .param("sourceIban", source.getIban().toMachineReadableString())
                .param("destinationIban", destination.getIban().toMachineReadableString()))
        .andExpect(status().isOk())
        .andReturn();

    var content = mvcResult.getResponse().getContentAsString();
    // basic sanity checks on returned payload
    assertThat(content).contains(destination.getIban().toMachineReadableString());
  }

  @Test
  @WithJwt("advisor.json")
  void givenInvalidTransferFilter_whenListTransfers_thenBadRequest() throws Exception {
    mockMvc
        .perform(
            get("https://localhost" + MoneyTransferController.BASE_PATH)
                .param("labelContaining", "ab"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenTransferMoney_thenUnauthorized() throws Exception {
    Account s1 = AccountFixtures.createCustomersXpfAccount(100000L);
    Account d1 = AccountFixtures.createSomeonesXpfAccount(50000L);

    var dto = new MoneyTransferRequest(
        s1.getIban().toMachineReadableString(),
        d1.getIban().toMachineReadableString(),
        1000L,
        s1.getBalance().getCurrencyIso3(),
        "label");

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithJwt("card-service.json")
  void givenServiceHasTransferAuthority_whenTransferMoney_thenCreated() throws Exception {
    Account source = AccountFixtures.createCustomersXpfAccount(100000L);
    Account destination = AccountFixtures.createSomeonesXpfAccount(50000L);

    var dto = new MoneyTransferRequest(
        source.getIban().toMachineReadableString(),
        destination.getIban().toMachineReadableString(),
        1000L,
        "XPF",
        "some label");

    when(accountRepo.findById(source.getIban())).thenReturn(Optional.of(source));
    when(accountRepo.findById(destination.getIban())).thenReturn(Optional.of(destination));
    when(transferRepo.save(any(MoneyTransfer.class))).thenAnswer(invocation -> {
      final var transfer = invocation.getArgument(0, MoneyTransfer.class);
      if (transfer.getId() == null) {
        transfer.setId(42L);
      }
      return transfer;
    });

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithJwt("card-service.json")
  void givenUnknownSource_whenTransferMoney_thenCreated() throws Exception {
    Account source = AccountFixtures.createCustomersXpfAccount(100000L);
    Account destination = AccountFixtures.createSomeonesXpfAccount(50000L);

    var dto = new MoneyTransferRequest(
        source.getIban().toMachineReadableString(),
        destination.getIban().toMachineReadableString(),
        1000L,
        "XPF",
        "label");

    when(accountRepo.findById(source.getIban())).thenReturn(Optional.empty());
    when(accountRepo.findById(destination.getIban())).thenReturn(Optional.of(destination));
    when(transferRepo.save(any(MoneyTransfer.class))).thenAnswer(invocation -> {
      final var transfer = invocation.getArgument(0, MoneyTransfer.class);
      if (transfer.getId() == null) {
        transfer.setId(42L);
      }
      return transfer;
    });

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithJwt("card-service.json")
  void givenUnknownDestination_whenTransferMoney_thenCreated() throws Exception {
    Account source = AccountFixtures.createCustomersXpfAccount(100000L);
    Iban destinationIban = AccountFixtures.createSomeonesXpfAccount(50000L).getIban();

    var dto = new MoneyTransferRequest(
        source.getIban().toMachineReadableString(),
        destinationIban.toMachineReadableString(),
        1000L,
        "XPF",
        "label");

    when(accountRepo.findById(source.getIban())).thenReturn(Optional.of(source));
    when(accountRepo.findById(destinationIban)).thenReturn(Optional.empty());
    when(transferRepo.save(any(MoneyTransfer.class))).thenAnswer(invocation -> {
      final var transfer = invocation.getArgument(0, MoneyTransfer.class);
      if (transfer.getId() == null) {
        transfer.setId(42L);
      }
      return transfer;
    });

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithJwt("card-service.json")
  void givenSourceAndDestinationHaveDifferentCurrencies_whenTransferMoney_thenConflict()
      throws Exception {
    Account source = AccountFixtures.createCustomersXpfAccount(100000L);
    Account destination = AccountFixtures.createCustomersEurAccount(50000L);

    var dto = new MoneyTransferRequest(
        source.getIban().toMachineReadableString(),
        destination.getIban().toMachineReadableString(),
        1000L,
        "XPF",
        "label");

    when(accountRepo.findById(source.getIban())).thenReturn(Optional.of(source));
    when(accountRepo.findById(destination.getIban())).thenReturn(Optional.of(destination));
    when(transferRepo.save(any(MoneyTransfer.class))).thenAnswer(invocation -> {
      final var transfer = invocation.getArgument(0, MoneyTransfer.class);
      if (transfer.getId() == null) {
        transfer.setId(42L);
      }
      return transfer;
    });

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithJwt("card-service.json")
  void givenSourceAnTransferHaveDifferentCurrencies_whenTransferMoney_thenConflict()
      throws Exception {
    Account source = AccountFixtures.createCustomersXpfAccount(100000L);
    Account destination = AccountFixtures.createCustomersXpfAccount(50000L);

    var dto = new MoneyTransferRequest(
        source.getIban().toMachineReadableString(),
        destination.getIban().toMachineReadableString(),
        1000L,
        "EUR",
        "label");

    when(accountRepo.findById(source.getIban())).thenReturn(Optional.of(source));
    when(accountRepo.findById(destination.getIban())).thenReturn(Optional.of(destination));

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isConflict());
  }

  @SuppressWarnings("null")
  @Test
  @WithJwt("card-service.json")
  void givenInvalidTransferRequest_whenTransferMoney_thenBadRequest() throws Exception {
    Account source = AccountFixtures.createCustomersXpfAccount(100000L);
    Account destination = AccountFixtures.createCustomersXpfAccount(50000L);

    when(accountRepo.findById(source.getIban())).thenReturn(Optional.of(source));
    when(accountRepo.findById(destination.getIban())).thenReturn(Optional.of(destination));

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json
                        .writeValueAsString(
                            new MoneyTransferRequest(
                                "not-an-iban",
                                destination.getIban().toMachineReadableString(),
                                1000L,
                                "XPF",
                                "label"))))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json
                        .writeValueAsString(
                            new MoneyTransferRequest(
                                source.getIban().toMachineReadableString(),
                                "not-an-iban",
                                1000L,
                                "XPF",
                                "label"))))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json
                        .writeValueAsString(
                            new MoneyTransferRequest(
                                source.getIban().toMachineReadableString(),
                                destination.getIban().toMachineReadableString(),
                                -1L,
                                "XPF",
                                "label"))))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json
                        .writeValueAsString(
                            new MoneyTransferRequest(
                                source.getIban().toMachineReadableString(),
                                destination.getIban().toMachineReadableString(),
                                1000L,
                                "XPFX",
                                "label"))))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            post("https://localhost" + MoneyTransferController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json
                        .writeValueAsString(
                            new MoneyTransferRequest(
                                source.getIban().toMachineReadableString(),
                                destination.getIban().toMachineReadableString(),
                                1000L,
                                "XPF",
                                "la"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsAdvisor_whenGetTransfer_thenOk() throws Exception {
    Account source3 = AccountFixtures.createCustomersXpfAccount(100000L);
    Account destination3 = AccountFixtures.createSomeonesXpfAccount(50000L);

    MoneyTransfer transfer2 =
        MoneyTransferFixtures.createMoneyTransfer(source3, destination3, 1000L);

    when(transferRepo.findById(1L)).thenReturn(Optional.of(transfer2));

    var actual = json
        .readValue(
            mockMvc
                .perform(get("https://localhost" + MoneyTransferController.TRANSFER_PATH, "1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MoneyTransferResponse.class);

    assertThat(actual.label()).contains("Test transfer of");
  }

  @Test
  @WithJwt("customer.json")
  void givenUserIsCustomer_whenGetSomeoneElsesTransfer_thenForbidden() throws Exception {
    var moneyTransferId = 2L;

    when(transferRepo.findById(moneyTransferId)).thenReturn(Optional.of(mock(MoneyTransfer.class)));

    mockMvc
        .perform(get("https://localhost" + MoneyTransferController.TRANSFER_PATH, moneyTransferId))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUnknownTransferId_whenGetTransfer_thenNotFound() throws Exception {
    when(transferRepo.findById(999L)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("https://localhost" + MoneyTransferController.TRANSFER_PATH, 999L))
        .andExpect(status().isNotFound());
  }

}
