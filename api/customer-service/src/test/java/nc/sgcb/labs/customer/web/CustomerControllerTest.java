package nc.sgcb.labs.customer.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithJwt;
import com.c4_soft.springaddons.security.oauth2.test.webmvc.AutoConfigureAddonsWebmvcResourceServerSecurity;
import nc.sgcb.labs.commons.exception.CommonExceptionsHandler;
import nc.sgcb.labs.customer.CustomerFixtures;
import nc.sgcb.labs.customer.SecurityConfig;
import nc.sgcb.labs.customer.SpringDataWebConvertersTestConfiguration;
import nc.sgcb.labs.customer.jpa.CustomerRepository;
import nc.sgcb.labs.user.api.UsersApi;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CustomerController.class, properties = {})
@Import({CustomerMapperImpl.class, CommonExceptionsHandler.class,
    SpringDataWebConvertersTestConfiguration.class, SecurityConfig.class})
@AutoConfigureAddonsWebmvcResourceServerSecurity
@SuppressWarnings("null")
class CustomerControllerTest {

  @MockitoBean
  CustomerRepository customerRepo;

  @MockitoBean
  UsersApi usersApi;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper json;

  // ===================== listCustomers =====================

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenListCustomers_thenUnauthorized() throws Exception {
    mockMvc
        .perform(
            get("https://localhost" + CustomerController.BASE_PATH)
                .queryParam("firstOrLastNamePart", "e"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsGrantedWithReadAny_whenListCustomersWithNamePart_thenOk() throws Exception {
    var customer = CustomerFixtures.createJeanBonot();
    when(customerRepo.findByFirstOrLastNameContainingIgnoreCase("bonot", PageRequest.of(0, 20)))
        .thenReturn(new PageImpl<>(List.of(customer), PageRequest.of(0, 20), 1));

    var mvcResult = mockMvc
        .perform(
            get("https://localhost" + CustomerController.BASE_PATH)
                .queryParam("firstOrLastNamePart", "bonot"))
        .andExpect(status().isOk())
        .andReturn();

    assertThat(mvcResult.getResponse().getContentAsString()).contains(customer.getId());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsGrantedWithReadAny_whenListCustomersWithBlankNamePart_thenFindAllIsUsed()
      throws Exception {
    var customer = CustomerFixtures.createJefHini();
    when(customerRepo.findAll(any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(customer)));

    mockMvc
        .perform(
            get("https://localhost" + CustomerController.BASE_PATH)
                .queryParam("firstOrLastNamePart", ""))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("https://localhost" + CustomerController.BASE_PATH))
        .andExpect(status().isOk());
  }

  // ===================== createCustomer =====================

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenCreateCustomer_thenUnauthorized() throws Exception {
    var dto = new CustomerCreationRequest(
        "Jean",
        "Bonot",
        LocalDate.of(1978, 10, 31),
        "Longjumeau (91)",
        "jean.bonot@test.pf");

    mockMvc
        .perform(
            post("https://localhost" + CustomerController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithJwt("advisor.json")
  void givenNewCustomer_whenCreateCustomer_thenCreated() throws Exception {
    var dto = new CustomerCreationRequest(
        "Jean",
        "Bonot",
        LocalDate.of(1978, 10, 31),
        "Longjumeau (91)",
        "jean.bonot@test.pf");

    when(
        customerRepo
            .existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
                dto.firstName(),
                dto.lastName(),
                dto.birthDate(),
                dto.birthLocation()))
        .thenReturn(false);

    var newUserId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    var headers = new HttpHeaders();
    headers.setLocation(URI.create("/users/%s".formatted(newUserId)));
    when(usersApi.createUser(any()))
        .thenReturn(new ResponseEntity<>(headers, org.springframework.http.HttpStatus.CREATED));

    when(customerRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    var mvcResult = mockMvc
        .perform(
            post("https://localhost" + CustomerController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andReturn();

    assertThat(mvcResult.getResponse().getHeader("Location")).contains(newUserId);
  }

  @Test
  @WithJwt("advisor.json")
  void givenExistingCustomer_whenCreateCustomer_thenConflict() throws Exception {
    var dto = new CustomerCreationRequest(
        "Jean",
        "Bonot",
        LocalDate.of(1978, 10, 31),
        "Longjumeau (91)",
        "jean.bonot@test.pf");

    when(
        customerRepo
            .existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
                dto.firstName(),
                dto.lastName(),
                dto.birthDate(),
                dto.birthLocation()))
        .thenReturn(true);

    mockMvc
        .perform(
            post("https://localhost" + CustomerController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithJwt("advisor.json")
  void givenInvalidPayload_whenCreateCustomer_thenBadRequest() throws Exception {
    // missing first name
    mockMvc
        .perform(
            post("https://localhost" + CustomerController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json
                        .writeValueAsString(
                            new CustomerCreationRequest(
                                "",
                                "Bonot",
                                LocalDate.of(1978, 10, 31),
                                "Longjumeau (91)",
                                "jean.bonot@test.pf"))))
        .andExpect(status().is4xxClientError());

    // invalid email
    mockMvc
        .perform(
            post("https://localhost" + CustomerController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json
                        .writeValueAsString(
                            new CustomerCreationRequest(
                                "Jean",
                                "Bonot",
                                LocalDate.of(1978, 10, 31),
                                "Longjumeau (91)",
                                "not-an-email"))))
        .andExpect(status().is4xxClientError());

    // missing birth date
    mockMvc
        .perform(
            post("https://localhost" + CustomerController.BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    json
                        .writeValueAsString(
                            new CustomerCreationRequest(
                                "Jean",
                                "Bonot",
                                null,
                                "Longjumeau (91)",
                                "jean.bonot@test.pf"))))
        .andExpect(status().is4xxClientError());
  }

  // ===================== getCustomer =====================

  @Test
  @WithAnonymousUser
  void givenAnonymousUser_whenGetCustomer_thenUnauthorized() throws Exception {
    mockMvc
        .perform(get("https://localhost" + CustomerController.CUSTOMER_PATH, "some-id"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUserIsGrantedWithReadAny_whenGetCustomerWithKnownCustomerId_thenOk() throws Exception {
    var customer = CustomerFixtures.createJeanBonot();
    when(customerRepo.findById(customer.getId())).thenReturn(Optional.of(customer));

    var mvcResult = mockMvc
        .perform(get("https://localhost" + CustomerController.CUSTOMER_PATH, customer.getId()))
        .andExpect(status().isOk())
        .andReturn();

    var actual =
        json.readValue(mvcResult.getResponse().getContentAsString(), CustomerResponse.class);
    assertThat(actual.id()).isEqualTo(customer.getId());
    assertThat(actual.firstName()).isEqualTo(customer.getFirstName());
  }

  @Test
  @WithJwt("john-deuf.json")
  void givenUserIsCustomer_whenGetCustomerWithCustomerId_thenOk() throws Exception {
    var customer = CustomerFixtures.createJohnDeuf();
    when(customerRepo.findById(customer.getId())).thenReturn(Optional.of(customer));

    var mvcResult = mockMvc
        .perform(get("https://localhost" + CustomerController.CUSTOMER_PATH, customer.getId()))
        .andExpect(status().isOk())
        .andReturn();

    var actual =
        json.readValue(mvcResult.getResponse().getContentAsString(), CustomerResponse.class);
    assertThat(actual.id()).isEqualTo(customer.getId());
    assertThat(actual.firstName()).isEqualTo(customer.getFirstName());
  }

  @Test
  @WithJwt("john-deuf.json")
  void givenUserIsCustomer_whenGetCustomerWithSomeoneElseId_thenForbiden() throws Exception {
    var jefHini = CustomerFixtures.createJefHini();
    when(customerRepo.findById(jefHini.getId())).thenReturn(Optional.of(jefHini));

    mockMvc
        .perform(get("https://localhost" + CustomerController.CUSTOMER_PATH, jefHini.getId()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithJwt("advisor.json")
  void givenUnknownCustomerId_whenGetCustomer_thenNotFound() throws Exception {
    when(customerRepo.findById("unknown-id")).thenReturn(Optional.empty());

    mockMvc
        .perform(get("https://localhost" + CustomerController.CUSTOMER_PATH, "unknown-id"))
        .andExpect(status().isNotFound());
  }

}
