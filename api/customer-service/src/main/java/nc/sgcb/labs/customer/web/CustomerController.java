package nc.sgcb.labs.customer.web;

import java.net.URI;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.customer.domain.Customer;
import nc.sgcb.labs.customer.jpa.CustomerJpaRepository;

@Tag(name = "Customers")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
@RequiredArgsConstructor
public class CustomerController {
  public static final String BASE_PATH = "/customers";
  public static final String CUSTOMER_ID_PLACEHOLDER = "customerId";
  public static final String CUSTOMER_PATH = BASE_PATH + "/{" + CUSTOMER_ID_PLACEHOLDER + "}";

  private final CustomerJpaRepository customerRepo;
  private final CustomerMapper customerMapper;

  @GetMapping(path = BASE_PATH)
  public PagedModel<CustomerResponse> listCustomers(
      @RequestParam String firstOrLastNamePart,
      @ParameterObject Pageable pageable) {
    final var customersPage = StringUtils.hasText(firstOrLastNamePart)
        ? customerRepo.findByFirstOrLastNameContainingIgnoreCase(firstOrLastNamePart, pageable)
        : customerRepo.findAll(pageable);
    return new PagedModel<>(customersPage.map(customerMapper::map));
  }

  @PostMapping(path = BASE_PATH)
  public ResponseEntity<Void> createCustomer(@RequestBody @Valid CustomerCreationRequest dto) {
    if (customerRepo
        .existsByFirstNameAndLastNameAndBirthDateAndBirthLocationAllIgnoreCase(
            dto.firstName(),
            dto.lastName(),
            dto.birthDate(),
            dto.birthLocation())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "User %s %s born on %s at %s already exists"
              .formatted(dto.firstName(), dto.lastName(), dto.birthDate(), dto.birthLocation()));
    }
    final var customer = customerRepo.save(customerMapper.map(dto));
    return ResponseEntity
        .created(
            URI
                .create(
                    CUSTOMER_PATH
                        .replace(
                            "{%s}".formatted(CUSTOMER_ID_PLACEHOLDER),
                            customer.getId().toString())))
        .build();
  }

  @GetMapping(path = CUSTOMER_PATH)
  public CustomerResponse getCustomer(
      @Parameter(schema = @Schema(type = "integer", format = "int64"))
      @PathVariable(CUSTOMER_ID_PLACEHOLDER) Customer customer) {
    return customerMapper.map(customer);
  }
}
