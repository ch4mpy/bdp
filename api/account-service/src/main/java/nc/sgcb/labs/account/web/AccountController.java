package nc.sgcb.labs.account.web;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.jpa.AccountJpaRepository;
import nc.sgcb.labs.account.jpa.MoneyTransferJpaRepository;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.exception.ResourceNotFoundException;
import nc.sgcb.labs.customer.api.CustomersApi;

@Tag(name = "Accounts")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
@RequiredArgsConstructor
public class AccountController {
  public static final String BASE_PATH = "/accounts";
  public static final String ACCOUNT_PLACEHOLDER = "iban";
  public static final String ACCOUNT_PATH = BASE_PATH + "/{" + ACCOUNT_PLACEHOLDER + "}";
  public static final String TRANSFER_LIST_PATH = ACCOUNT_PATH + "/transfers";
  public static final String WITHDRAW_PATH = ACCOUNT_PATH + "/withdraw";
  public static final String CREDIT_PATH = ACCOUNT_PATH + "/credit";

  private final AccountJpaRepository accountRepo;
  private final AccountMapper accountMapper;

  private final MoneyTransferJpaRepository transferRepo;
  private final MoneyTransferMapper transferMapper;

  private final CustomersApi customersApi;

  @GetMapping(BASE_PATH)
  public List<AccountResponse> listAccounts(@RequestParam Long customerId) {
    final var accounts = accountRepo.findByCustomerId(customerId);
    return accounts.stream().map(accountMapper::map).toList();
  }

  @PostMapping(BASE_PATH)
  public ResponseEntity<Void> createAccount(@RequestBody @Valid AccountCreationRequest dto)
      throws ResourceNotFoundException {
    final var iban = Iban.parse(dto.iban());

    // Assert that no account with this IBAN is managed already
    if (accountRepo.existsById(iban)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "The account-service already manages account %s".formatted(iban.toHumanReadableString()));
    }

    // Assert that the customer ID is known by the customer service
    try {
      customersApi.getCustomer(dto.customerId());
    } catch (HttpClientErrorException e) {
      if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
        throw new ResourceNotFoundException(
            "Customer %s is not known by the customer-service".formatted(dto.iban()));
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

  @GetMapping(ACCOUNT_PATH)
  public AccountResponse getAccount(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = ACCOUNT_PLACEHOLDER) Account account) {
    return accountMapper.map(account);
  }

  @GetMapping(TRANSFER_LIST_PATH)
  public PagedModel<MoneyTransferResponse> listAccountTransfers(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = ACCOUNT_PLACEHOLDER) Account account,
      @Valid @ParameterObject MoneyTransferFilterRequest dto,
      @ParameterObject Pageable pageable) {
    var criteria = transferMapper.map(dto);
    var transferPage =
        transferRepo.findAll(MoneyTransferJpaRepository.searchSpec(criteria), pageable);
    var content = transferPage.getContent().stream().map(transferMapper::map).toList();
    return new PagedModel<>(
        new PageImpl<>(content, transferPage.getPageable(), transferPage.getTotalElements()));
  }

  @PostMapping(WITHDRAW_PATH)
  @ResponseStatus(code = HttpStatus.ACCEPTED)
  public void withdrawAccount(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = ACCOUNT_PLACEHOLDER) Account account,
      @Valid AccountWithdrawRequest dto) {
    assert Objects.equals(account.getBalance().getCurrencyIso3(), dto.currency());
    transferRepo
        .save(
            MoneyTransfer
                .builder()
                .amount(new Amount(dto.currency(), dto.amount()))
                .fromIban(account.getIban())
                .toIban(Iban.parse(dto.toIban()))
                .label(dto.label())
                .build());
  }

  @PostMapping(CREDIT_PATH)
  public ResponseEntity<Void> creditAccount(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = ACCOUNT_PLACEHOLDER) Account account,
      @Valid AccountCreditRequest dto) {
    assert Objects.equals(account.getBalance().getCurrencyIso3(), dto.currency());
    transferRepo
        .save(
            MoneyTransfer
                .builder()
                .amount(new Amount(dto.currency(), dto.amount()))
                .fromIban(Iban.parse(dto.fromIban()))
                .toIban(account.getIban())
                .label(dto.label())
                .build());
    return ResponseEntity.accepted().build();
  }

}
