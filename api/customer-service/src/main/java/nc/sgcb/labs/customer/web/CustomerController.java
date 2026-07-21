package nc.sgcb.labs.customer.web;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.exception.InternalServerErrorException;
import nc.sgcb.labs.commons.exception.ResourceNotFoundException;
import nc.sgcb.labs.customer.domain.Beneficiary;
import nc.sgcb.labs.customer.domain.Customer;
import nc.sgcb.labs.customer.jpa.BeneficiaryRepository;
import nc.sgcb.labs.customer.jpa.CustomerRepository;
import nc.sgcb.labs.user.api.UsersApi;
import nc.sgcb.labs.user.model.UserRequest;

@Tag(name = "Customers")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
@RequiredArgsConstructor
@Observed
@Slf4j
public class CustomerController {
  public static final String BASE_PATH = "/customers";
  public static final String CUSTOMER_ID_PLACEHOLDER = "customerId";
  public static final String CUSTOMER_PATH = BASE_PATH + "/{" + CUSTOMER_ID_PLACEHOLDER + "}";
  public static final String BENEFICIARIES_PATH = CUSTOMER_PATH + "/beneficiaries";
  public static final String BENEFICIARY_ID_PLACEHOLDER = "beneficiaryId";
  public static final String BENEFICIARY_PATH =
      BENEFICIARIES_PATH + "/{" + BENEFICIARY_ID_PLACEHOLDER + "}";

  private final CustomerRepository customerRepo;
  private final CustomerMapper customerMapper;

  private final BeneficiaryRepository beneficiaryRepo;

  private final UsersApi usersApi;

  /**
   * Requires the `customer.read_any` authority
   * 
   * @param firstOrLastNamePart optional part of first or last name to filter by
   * @param pageable
   * @return
   */
  @Transactional(readOnly = true)
  @GetMapping(path = BASE_PATH)
  @PreAuthorize("hasAuthority('customer.read_any')")
  public PagedModel<CustomerResponse> listCustomers(
      @RequestParam(required = false) @Nullable String firstOrLastNamePart,
      @ParameterObject Pageable pageable) {
    log
        .debug(
            "Listing customers with firstOrLastNamePart={} and pageable={}",
            firstOrLastNamePart,
            pageable);
    final var customersPage = StringUtils.hasText(firstOrLastNamePart)
        ? customerRepo.findByFirstOrLastNameContainingIgnoreCase(firstOrLastNamePart, pageable)
        : customerRepo.findAll(pageable);
    return new PagedModel<>(customersPage.map(customerMapper::map));
  }

  /**
   * Requires the `customer.edit` authority
   * 
   * @param dto
   * @return
   */
  @Transactional
  @PostMapping(path = BASE_PATH)
  @PreAuthorize("hasAuthority('customer.edit')")
  public ResponseEntity<Void> createCustomer(
      @RequestBody @Valid CustomerCreationRequest dto,
      Authentication auth) {
    if (customerRepo
        .existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
            dto.firstName(),
            dto.lastName(),
            dto.birthDate(),
            dto.birthLocation())) {
      log
          .warn(
              "Customer {} {} born on {} at {} already exists. Rejecting creation.",
              dto.firstName(),
              dto.lastName(),
              dto.birthDate(),
              dto.birthLocation());
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Customer %s %s born on %s at %s already exists"
              .formatted(dto.firstName(), dto.lastName(), dto.birthDate(), dto.birthLocation()));
    }

    final var username = "%s.%s".formatted(dto.firstName(), dto.lastName()).toLowerCase();
    log
        .debug(
            "Calling the users API to create {} {} {} {}",
            dto.firstName(),
            dto.lastName(),
            username,
            dto.email());
    final var userCreationResponse = usersApi
        .createUser(
            new UserRequest()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .username(username)
                .email(dto.email()));
    log
        .debug(
            "User created by the users API at {}",
            userCreationResponse.getHeaders().getLocation());
    final var pathParts = userCreationResponse.getHeaders().getLocation().getPath().split("/");
    final var sub = pathParts[pathParts.length - 1];
    log.info("{} created user {}", auth.getName(), sub);
    customerRepo.save(customerMapper.map(dto, sub));
    log.debug("Customer created for user {}", sub);
    return ResponseEntity
        .created(URI.create(CUSTOMER_PATH.replace("{%s}".formatted(CUSTOMER_ID_PLACEHOLDER), sub)))
        .build();
  }

  /**
   * Requires the `customer.read_any` authority or the user to be the customer
   * 
   * @param customer
   * @return
   */
  @Transactional(readOnly = true)
  @GetMapping(path = CUSTOMER_PATH)
  @PreAuthorize("hasAuthority('customer.read_any') or #customer.id == authentication.name")
  public CustomerResponse getCustomer(
      @Parameter(schema = @Schema(type = "string"),
          description = "The ID of the customer to retrieve")
      @PathVariable(CUSTOMER_ID_PLACEHOLDER) Customer customer) {
    return customerMapper.map(customer);
  }

  /**
   * Requires the `customer.read_any` authority or the user to be the customer
   * 
   * @param customer
   * @return
   */
  @Transactional(readOnly = true)
  @GetMapping(path = BENEFICIARIES_PATH)
  @PreAuthorize("hasAuthority('customer.read_any') or #customer.id == authentication.name")
  public List<BeneficiaryResponse> listBeneficiaries(
      @Parameter(schema = @Schema(type = "string"),
          description = "The ID of the customer to retrieve")
      @PathVariable(CUSTOMER_ID_PLACEHOLDER) Customer customer) {
    return customer.getBeneficiaries().stream().map(customerMapper::map).toList();
  }

  /**
   * Requires the `customer.edit` authority or the user to be the customer
   * 
   * @param customer
   * @param dto Neither IBAN nor label can be used for this customer
   * @return a response with a Location header pointing to the created beneficiary
   * @throws InternalServerErrorException
   */
  @Transactional(readOnly = false)
  @PostMapping(path = BENEFICIARIES_PATH)
  @PreAuthorize("hasAuthority('customer.edit') or #customer.id == authentication.name")
  public ResponseEntity<Void> addBeneficiary(
      @Parameter(schema = @Schema(type = "string"),
          description = "The ID of the customer to retrieve")
      @PathVariable(CUSTOMER_ID_PLACEHOLDER) Customer customer,
      @RequestBody @Valid BeneficiaryRequest dto,
      Authentication auth) throws InternalServerErrorException {
    final var iban = Iban.of(dto.iban());
    final var label = dto.label().trim();
    assertIbanNotKnown(customer.getBeneficiaries().stream(), iban);
    assertLabelNotKnown(customer.getBeneficiaries().stream(), label);
    final var beneficiary = beneficiaryRepo.save(customerMapper.map(dto, customer));
    log.info("{} created beneficiary {} for customer {}", auth.getName(), beneficiary, customer);
    return ResponseEntity
        .created(
            URI
                .create(
                    BENEFICIARY_PATH
                        .replace("{%s}".formatted(CUSTOMER_ID_PLACEHOLDER), customer.getId())
                        .replace(
                            "{%s}".formatted(BENEFICIARY_ID_PLACEHOLDER),
                            beneficiary.getId().toString())))
        .build();
  }

  @Transactional(readOnly = true)
  @GetMapping(path = BENEFICIARY_PATH)
  @PreAuthorize("hasAuthority('customer.read_any') or #customer.id == authentication.name")
  public BeneficiaryResponse getBeneficiary(
      @Parameter(schema = @Schema(type = "string"),
          description = "The ID of the customer to retrieve")
      @PathVariable(CUSTOMER_ID_PLACEHOLDER) Customer customer,
      @Parameter(schema = @Schema(type = "integer", format = "int64"),
          description = "The ID of the customer to retrieve")
      @PathVariable(BENEFICIARY_ID_PLACEHOLDER) Beneficiary beneficiary)
      throws ResourceNotFoundException {
    assertCustomerBeneficiaryConsistency(customer, beneficiary);
    return customerMapper.map(beneficiary);
  }

  @Transactional(readOnly = false)
  @PutMapping(path = BENEFICIARY_PATH)
  @PreAuthorize("hasAuthority('customer.edit') or #customer.id == authentication.name")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void updateBeneficiary(
      @Parameter(schema = @Schema(type = "string"),
          description = "The ID of the customer to retrieve")
      @PathVariable(CUSTOMER_ID_PLACEHOLDER) Customer customer,
      @Parameter(schema = @Schema(type = "integer", format = "int64"),
          description = "The ID of the customer to retrieve")
      @PathVariable(BENEFICIARY_ID_PLACEHOLDER) Beneficiary beneficiary,
      @RequestBody @Valid BeneficiaryRequest dto,
      Authentication auth) throws ResourceNotFoundException {
    assertCustomerBeneficiaryConsistency(customer, beneficiary);
    var iban = Iban.of(dto.iban());
    var label = dto.label().trim();
    assertIbanNotKnown(otherCustomerBeneficiaries(customer, beneficiary), iban);
    assertLabelNotKnown(otherCustomerBeneficiaries(customer, beneficiary), label);
    beneficiary.setIban(iban);
    beneficiary.setLabel(label);
    beneficiaryRepo.save(beneficiary);
    log.info("{} updated beneficiary {} for customer {}", auth.getName(), beneficiary, customer);
  }

  @Transactional(readOnly = false)
  @DeleteMapping(path = BENEFICIARY_PATH)
  @PreAuthorize("hasAuthority('customer.edit') or #customer.id == authentication.name")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void deleteBeneficiary(
      @Parameter(schema = @Schema(type = "string"),
          description = "The ID of the customer to retrieve")
      @PathVariable(CUSTOMER_ID_PLACEHOLDER) Customer customer,
      @Parameter(schema = @Schema(type = "integer", format = "int64"),
          description = "The ID of the customer to retrieve")
      @PathVariable(BENEFICIARY_ID_PLACEHOLDER) Beneficiary beneficiary,
      Authentication auth) throws ResourceNotFoundException {
    assertCustomerBeneficiaryConsistency(customer, beneficiary);
    customer.getBeneficiaries().remove(beneficiary);
    customerRepo.save(customer);
    beneficiaryRepo.delete(beneficiary);
    log.info("{} deleted beneficiary {} for customer {}", auth.getName(), beneficiary, customer);
  }

  private void assertIbanNotKnown(Stream<Beneficiary> beneficiariesStream, Iban iban) {
    if (beneficiariesStream.anyMatch(b -> Objects.equals(b.getIban(), iban))) {
      log.warn("The customer already has a beneficiary with IBAN {}", iban);
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "A beneficiary is already registered with IBAN %s".formatted(iban));
    }
  }

  private void assertLabelNotKnown(Stream<Beneficiary> beneficiariesStream, String label) {
    final var needle = label.trim().toLowerCase();
    if (beneficiariesStream.anyMatch(b -> Objects.equals(b.getLabel().toLowerCase(), needle))) {
      log.warn("The customer already has a beneficiary with {} as label", needle);
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "A beneficiary is already registered with a label similar to %s".formatted(label));
    }
  }

  private void assertCustomerBeneficiaryConsistency(Customer customer, Beneficiary beneficiary)
      throws ResourceNotFoundException {
    if (!Objects.equals(customer, beneficiary.getCustomer())) {
      log.warn("Customer {} and beneficiary {} do not match", customer, beneficiary);
      throw new ResourceNotFoundException(
          "No beneficiary with ID %d for customer %s"
              .formatted(beneficiary.getId(), customer.getId()));
    }
  }

  private Stream<Beneficiary> otherCustomerBeneficiaries(
      Customer customer,
      Beneficiary beneficiary) {
    return customer
        .getBeneficiaries()
        .stream()
        .filter(b -> !Objects.equals(b.getId(), beneficiary.getId()));
  }
}
