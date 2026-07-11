package nc.sgcb.labs.account.web;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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

  @GetMapping(BASE_PATH)
  public List<AccountResponse> listAccounts(@RequestParam Long customerId) {
    final var accounts = accountRepo.findByCustomerId(customerId);
    return accounts.stream().map(accountMapper::map).toList();
  }

  @GetMapping(ACCOUNT_PATH)
  public AccountResponse getAccount(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = ACCOUNT_PLACEHOLDER) Account account) {
    return accountMapper.map(account);
  }

  @GetMapping(TRANSFER_LIST_PATH)
  public PagedModel<MoneyTransferResponse> listTransfers(
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
  public void withdraw(
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
  public ResponseEntity<Void> credit(
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
