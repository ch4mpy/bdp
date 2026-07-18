package nc.sgcb.labs.account.web;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.account.jpa.AccountRepository;
import nc.sgcb.labs.account.jpa.MoneyTransferRepository;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;

@Tag(name = "MoneyTransfers")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
@RequiredArgsConstructor
@Observed
@Slf4j
public class MoneyTransferController {
  public static final String BASE_PATH = "/transfers";
  public static final String TRANSFER_ID_PLACEHOLDER = "transferId";
  public static final String TRANSFER_PATH = BASE_PATH + "/{" + TRANSFER_ID_PLACEHOLDER + "}";

  private final AccountRepository accountRepo;

  private final MoneyTransferRepository transferRepo;
  private final MoneyTransferMapper transferMapper;

  /**
   * Requires the `account.read_any` authority or that the authenticated user is the owner of the
   * source or destination account.
   *
   * @param dto the filter criteria for money transfers
   * @param pageable the pagination information
   * @return a paginated list of money transfers matching the filter criteria
   */
  @Transactional(readOnly = true)
  @GetMapping(BASE_PATH)
  @PreAuthorize("hasAuthority('account.read_any') or @ac.ownsAccount(#dto.sourceIban) or @ac.ownsAccount(#dto.destinationIban)")
  public PagedModel<MoneyTransferResponse> listMoneyTransfers(
      @Nullable @Valid @ParameterObject MoneyTransferFilterRequest dto,
      @ParameterObject Pageable pageable) {
    var criteria = transferMapper.map(dto == null ? MoneyTransferFilterRequest.ALL : dto);
    var transferPage = transferRepo.findAll(MoneyTransferRepository.searchSpec(criteria), pageable);
    var content = transferPage.getContent().stream().map(transferMapper::map).toList();
    return new PagedModel<>(
        new PageImpl<>(content, transferPage.getPageable(), transferPage.getTotalElements()));
  }

  /**
   * Requires the `account.transfer` authority.
   *
   * @param dto the money transfer request
   * @param auth the authentication object representing the current user
   */
  @Transactional
  @PostMapping(BASE_PATH)
  @ResponseStatus(code = HttpStatus.ACCEPTED)
  @PreAuthorize("hasAuthority('account.transfer')")
  public void transferMoneyBetweenAccounts(
      @RequestBody @Valid MoneyTransferRequest dto,
      Authentication auth) {
    final var sourceAccount = accountRepo.findById(Iban.of(dto.sourceIban())).orElseThrow(() -> {
      log
          .warn(
              "{} attempting a transfer from unknown account {}",
              auth.getName(),
              dto.sourceIban());
      return new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Source account %s is not known".formatted(dto.sourceIban()));
    });
    final var destinationAccount =
        accountRepo.findById(Iban.of(dto.destinationIban())).orElseThrow(() -> {
          log
              .warn(
                  "{} attempting a transfer to unknown account {}",
                  auth.getName(),
                  dto.sourceIban());
          return new ResponseStatusException(
              HttpStatus.NOT_FOUND,
              "Destination account %s is not known".formatted(dto.destinationIban()));
        });

    if (!Objects.equals(sourceAccount.getBalance().getCurrencyIso3(), dto.currency())
        || !Objects.equals(destinationAccount.getBalance().getCurrencyIso3(), dto.currency())) {
      log
          .warn(
              "{} attempting a transfer in {} which is not the same as source's ({}) or destination's ({}) currencies",
              auth.getName(),
              dto.currency(),
              sourceAccount.getBalance().getCurrencyIso3(),
              destinationAccount.getBalance().getCurrencyIso3());
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "The transfer currency (%s) must be the same as source's (%s) and destination's (%s) ones"
              .formatted(
                  sourceAccount.getBalance().getCurrencyIso3(),
                  destinationAccount.getBalance().getCurrencyIso3(),
                  dto.currency()));
    }

    log
        .info(
            "Transfering {} {} from {} to {}",
            dto.amount(),
            dto.currency(),
            sourceAccount.getIban(),
            destinationAccount.getIban());
    sourceAccount.getBalance().setDigits(sourceAccount.getBalance().getDigits() - dto.amount());
    destinationAccount
        .getBalance()
        .setDigits(sourceAccount.getBalance().getDigits() + dto.amount());

    accountRepo.save(sourceAccount);
    log
        .debug(
            "Source account {} balance updated to {}",
            sourceAccount.getIban(),
            sourceAccount.getBalance().getDigits());
    accountRepo.save(destinationAccount);
    log
        .debug(
            "Destination account {} balance updated to {}",
            destinationAccount.getIban(),
            destinationAccount.getBalance().getDigits());
    transferRepo
        .save(
            MoneyTransfer
                .builder()
                .amount(new Amount(dto.currency(), dto.amount()))
                .sourceIban(sourceAccount.getIban())
                .destinationIban(destinationAccount.getIban())
                .label(dto.label())
                .build());
    log
        .debug(
            "Transfer from {} to {} of {} {} saved",
            sourceAccount.getIban(),
            destinationAccount.getIban(),
            dto.amount(),
            dto.currency());
  }

  /**
   * Requires the `account.read_any` authority or that the authenticated user is the owner of the
   * source or destination account.
   *
   * @param transfer
   * @return the money transfer with the given ID
   */
  @Transactional(readOnly = true)
  @GetMapping(TRANSFER_PATH)
  @PreAuthorize("hasAuthority('account.read_any') or @ac.ownsAccount(#transfer.sourceIban) or @ac.ownsAccount(#transfer.destinationIban)")
  public MoneyTransferResponse getMoneyTransfer(
      @Parameter(schema = @Schema(type = "integer"),
          description = "The ID of the money transfer to retrieve")
      @PathVariable(name = TRANSFER_ID_PLACEHOLDER) MoneyTransfer transfer) {
    return transferMapper.map(transfer);
  }

}
