package nc.sgcb.labs.account.web;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.jpa.AccountRepository;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.exception.ResourceNotFoundException;
import nc.sgcb.labs.customer.api.CustomersApi;

@Tag(name = "Accounts")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
@RequiredArgsConstructor
@Observed
@Slf4j
public class AccountController {
  public static final String BASE_PATH = "/accounts";
  public static final String ACCOUNT_PLACEHOLDER = "iban";
  public static final String ACCOUNT_PATH = BASE_PATH + "/{" + ACCOUNT_PLACEHOLDER + "}";

  private final AccountRepository accountRepo;
  private final AccountMapper accountMapper;

  private final CustomersApi customersApi;

  /**
   * Requires the `account.read_any` authority or that the given customer ID matches the
   * authenticated user.
   * 
   * @param customerId
   * 
   * @return all accounts with the given customer ID.
   */
  @Transactional(readOnly = true)
  @GetMapping(BASE_PATH)
  @PreAuthorize("hasAuthority('account.read_any') || #customerId == authentication.name")
  public List<AccountResponse> listAccounts(@RequestParam @NotEmpty String customerId) {
    final var accounts = accountRepo.findByCustomerId(customerId);
    return accounts.stream().map(accountMapper::map).toList();
  }

  /**
   * Requires the `account.create` authority.
   * 
   * @param dto
   * @return a 201 Created response with the Location header set to the newly created account's URL.
   * @throws ResourceNotFoundException if the customer ID in the request is not known by the
   *         customer service.
   */
  @Transactional
  @PostMapping(BASE_PATH)
  @PreAuthorize("hasAuthority('account.create')")
  public ResponseEntity<Void> createAccount(@RequestBody @Valid AccountCreationRequest dto)
      throws ResourceNotFoundException {
    final var iban = Iban.of(dto.iban());

    // Assert that no account with this IBAN is managed already
    if (accountRepo.existsById(iban)) {
      log.warn("Rejecting duplicate account {} creation", iban);
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "The account-service already manages account %s".formatted(iban.toHumanReadableString()));
    }

    // Assert that the customer ID is known by the customer service
    try {
      customersApi.getCustomer(dto.customerId());
    } catch (HttpClientErrorException e) {
      if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
        log.warn("Rejecting account {} creation for unknown customer {}", iban, dto.customerId());
        throw new ResourceNotFoundException(
            "Customer %s is not known by the customer-service".formatted(dto.iban()));
      } else {
        log
            .error(
                "Unexpected error while checking customer {} existence in customer-service",
                dto.customerId(),
                e);
      }
      throw e;
    }

    // Create the new account
    final var account = accountRepo
        .save(
            Account
                .builder()
                .customerId(dto.customerId())
                .iban(iban)
                .balance(Amount.builder().currencyIso3(dto.currency()).digits(0L).build())
                .build());
    log.info("Created new account {} for customer {}", account.getIban(), account.getCustomerId());

    return ResponseEntity
        .created(
            URI
                .create(
                    ACCOUNT_PATH
                        .replace(
                            "{%s}".formatted(ACCOUNT_PLACEHOLDER),
                            account.getIban().toMachineReadableString())))
        .build();
  }

  /**
   * Requires the `account.read_any` authority or that the given account's customer ID matches the
   * authenticated user.
   * 
   * @param account
   * @return the account with the given IBAN.
   */
  @Transactional(readOnly = true)
  @GetMapping(ACCOUNT_PATH)
  @PreAuthorize("hasAuthority('account.read_any') || #account.customerId == authentication.name")
  public AccountResponse getAccount(
      @Parameter(schema = @Schema(type = "string"),
          description = "The IBAN of the account to retrieve")
      @PathVariable(name = ACCOUNT_PLACEHOLDER) Account account) {
    return accountMapper.map(account);
  }

}
