package nc.sgcb.labs.card.payment.web;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.sgcb.labs.account.api.AccountsApi;
import nc.sgcb.labs.account.api.MoneyTransfersApi;
import nc.sgcb.labs.account.model.MoneyTransferRequest;
import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.card.payment.domain.Card.Ceilings;
import nc.sgcb.labs.card.payment.domain.CardPayment;
import nc.sgcb.labs.card.payment.jpa.CardPaymentRepository;
import nc.sgcb.labs.card.payment.jpa.CardRepository;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.domain.Period;
import nc.sgcb.labs.commons.exception.InternalServerErrorException;
import nc.sgcb.labs.commons.exception.ResourceNotFoundException;
import nc.sgcb.labs.commons.validation.ValidPeriod;

@Tag(name = "Cards")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
@RequiredArgsConstructor
@Observed
@Slf4j
public class CardController {
  public static final String BASE_PATH = "/cards";
  public static final String CARD_NUMBER_PLACEHOLDER = "cardNumber";
  public static final String CARD_PATH = BASE_PATH + "/{" + CARD_NUMBER_PLACEHOLDER + "}";
  public static final String CARD_STATUS_PATH = CARD_PATH + "/status";
  public static final String CARD_CEILINGS_PATH = CARD_PATH + "/ceilings";
  public static final String PAYMENT_LIST_PATH = CARD_PATH + "/payments";
  public static final String PAYMENT_ID_PLACEHOLDER = "paymentId";
  public static final String PAYMENT_PATH = PAYMENT_LIST_PATH + "/{" + PAYMENT_ID_PLACEHOLDER + "}";

  private final CardRepository cardRepo;
  private final CardMapper cardMapper;
  private final CardPaymentRepository paymentRepo;
  private final CardPaymentMapper paymentMapper;
  private final AccountsApi accountsApi;
  private final MoneyTransfersApi transfersApi;

  /**
   * Requires the `card.read_any` authority or that the authenticated user is the owner of the
   * account.
   * 
   * @param iban
   * @return
   */
  @Transactional(readOnly = true)
  @GetMapping(path = BASE_PATH)
  @PreAuthorize("hasAuthority('card.read_any') or @ac.ownsAccount(#iban.toMachineReadableString())")
  public List<CardResponse> listCards(@RequestParam Iban iban) {
    var cards = cardRepo.findByIban(iban);
    return cards.stream().map(cardMapper::map).toList();
  }

  /**
   * Requires the `card.create` authority.
   * 
   * @param dto
   * @param auth
   * @return a response with a `Location` header pointing to the newly created card resource
   * @throws ResourceNotFoundException if the account is not known by the account service
   */
  @Transactional
  @PostMapping(path = BASE_PATH)
  @PreAuthorize("hasAuthority('card.create')")
  public ResponseEntity<Void> createCard(
      @RequestBody @Valid CardRequest dto,
      Authentication auth) throws ResourceNotFoundException {
    // Assert that the account is known by the account service
    try {
      accountsApi.getAccount(dto.iban());
    } catch (HttpClientErrorException e) {
      if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
        log.warn("{} atempted to create a card for unknown account {}", auth.getName(), dto.iban());
        throw new ResourceNotFoundException(
            "%s account is not known to the account-service".formatted(dto.iban()));
      }
      log
          .error(
              "Error while checking account {} existence for user {}",
              dto.iban(),
              auth.getName(),
              e);
      throw e;
    }
    var iban = Iban.of(dto.iban());
    var existingCards = cardRepo.findByIban(iban);
    var cardNumber = "4%s%d".formatted(iban.getBban(), existingCards.size());

    var card = cardRepo
        .save(
            Card
                .builder()
                .number(cardNumber)
                .iban(iban)
                .ceilings(
                    Ceilings
                        .builder()
                        .rolling30(dto.rolling30Ceiling())
                        .transaction(dto.transactionCeiling())
                        .build())
                .active(true)
                .build());
    log.info("{} created card {} for account {}", auth.getName(), card.getNumber(), dto.iban());

    return ResponseEntity
        .created(
            URI
                .create(
                    CARD_PATH.replace("{%s}".formatted(CARD_NUMBER_PLACEHOLDER), card.getNumber())))
        .build();
  }

  /**
   * Requires the `card.read_any` authority or that the authenticated user is the owner of the card
   * 
   * @param card
   * @return
   */
  @Transactional(readOnly = true)
  @GetMapping(path = CARD_PATH)
  @PreAuthorize("hasAuthority('card.read_any') or @ac.ownsAccount(#card.getIban().toMachineReadableString())")
  public CardResponse getCard(
      @Parameter(schema = @Schema(type = "string"),
          description = "The number of the card to change the status of")
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card) {
    return cardMapper.map(card);
  }

  /**
   * Requires the `card.card-status_edit` authority
   * 
   * @param card
   * @param dto
   * @param auth
   */
  @Transactional
  @PutMapping(path = CARD_STATUS_PATH)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("hasAuthority('card.card-status_edit')")
  public void setCardStatus(
      @Parameter(schema = @Schema(type = "string"),
          description = "The number of the card to change the status of")
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @RequestBody @Valid CardStatusRequest dto,
      Authentication auth) {
    log
        .info(
            "{} is changing status {} card status from {} to {}",
            auth.getName(),
            card.getNumber(),
            card.isActive(),
            dto.isActive());
    card.setActive(dto.isActive());
    cardRepo.save(card);
    log.debug("{} changed card {} status to {}", auth.getName(), card.getNumber(), dto.isActive());
  }

  /**
   * Requires the `card.ceilings_edit` authority
   * 
   * @param card
   * @param dto
   * @param auth
   */
  @Transactional
  @PutMapping(path = CARD_CEILINGS_PATH)
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("hasAuthority('card.ceilings_edit')")
  public void setCardCeilings(
      @Parameter(schema = @Schema(type = "string"),
          description = "The number of the card to change the ceilings of")
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @RequestBody @Valid CardCeilingsRequest dto,
      Authentication auth) {
    var newCeilings = Ceilings
        .builder()
        .rolling30(dto.rolling30Ceiling())
        .transaction(dto.transactionCeiling())
        .build();
    log
        .info(
            "{} is changing card {} ceilings from {} to {}",
            auth.getName(),
            card.getNumber(),
            card.getCeilings(),
            newCeilings);
    card.setCeilings(newCeilings);
    cardRepo.save(card);
    log.debug("{} changed card {} ceilings to {}", auth.getName(), card.getNumber(), newCeilings);
  }

  /**
   * Requires the `card.read_any` authority or that the authenticated user is the owner of the card
   * 
   * @param card
   * @param period must be at most 61 days long
   * @return
   */
  @Transactional(readOnly = true)
  @GetMapping(path = PAYMENT_LIST_PATH)
  @PreAuthorize("hasAuthority('card.read_any') or @ac.ownsAccount(#card.getIban().toMachineReadableString())")
  public List<CardPaymentResponse> listCardPayments(
      @Parameter(schema = @Schema(type = "string"),
          description = "The number of the card to retrieve the payments of")
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @ParameterObject @NotNull @ValidPeriod(maxSeconds = 3600 * 24 * 61) Period period) {
    return paymentRepo
        .findByCardNumberAndTimestampBetween(card.getNumber(), period.from(), period.to())
        .stream()
        .map(paymentMapper::map)
        .toList();
  }

  /**
   * Requires the authenticated user is the owner of the card
   * 
   * @param card
   * @param dto
   * @return
   * @throws ResourceNotFoundException if the destination account is not known by the account
   *         service
   * @throws InternalServerErrorException
   */
  @PostMapping(path = PAYMENT_LIST_PATH)
  @PreAuthorize("@ac.ownsAccount(#card.getIban().toMachineReadableString())")
  public ResponseEntity<Void> createCardPayment(
      @Parameter(schema = @Schema(type = "string",
          description = "The number of the card to create a payment with"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @RequestBody @Valid CardPaymentCreationRequest dto)
      throws ResourceNotFoundException, InternalServerErrorException {
    // Assert that the destination account is known by the account service
    try {
      log.debug("Retrieving account {} from the account service", dto.destinationIban());
      final var destinationAccount = accountsApi.getAccount(dto.destinationIban()).getBody();

      if (!Objects.equals(destinationAccount.getCurrency(), dto.currency())) {
        log
            .warn(
                "Card payment with card {} to account {} rejected because the account's currency {} does not match the payment's currency {}",
                card.getNumber(),
                destinationAccount.getIban(),
                destinationAccount.getCurrency(),
                dto.currency());
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Card payments are currently accepted only in the %s account's currency: %s"
                .formatted(destinationAccount.getIban(), destinationAccount.getCurrency()));
      }

      if (card.getCeilings().getTransaction().compareTo(dto.amount()) < 0) {
        log
            .warn(
                "Card payment with card {} to account {} rejected because the transaction ceiling of %d does not allow this payment of %d",
                card.getNumber(),
                destinationAccount.getIban(),
                card.getCeilings().getTransaction(),
                dto.amount());
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Card transaction ceiling set at %d does not allow this payment of %d"
                .formatted(card.getCeilings().getTransaction(), dto.amount()));
      }

      final var cumulatedAmount = getAcceptedPaymentsCumulatedAmountOn30Days(card);
      if (card.getCeilings().getRolling30().compareTo(cumulatedAmount + dto.amount()) < 0) {
        log
            .warn(
                "Card payment with card {} to account {} rejected because the rolling30 ceiling of %d does not allow this payment of %d because the cumulated amount of accepted payments is %d",
                card.getNumber(),
                destinationAccount.getIban(),
                card.getCeilings().getRolling30(),
                dto.amount(),
                cumulatedAmount);
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Card rolling30 ceiling set at %d does not allow this payment of %d because the cumulated amount of accepted payments is %d"
                .formatted(card.getCeilings().getRolling30(), dto.amount(), cumulatedAmount));
      }

      var payment = createPayemnt(card, dto);
      log
          .debug(
              "Created card payment {} with card {} to account {}",
              payment.getId(),
              card.getNumber(),
              destinationAccount.getIban());

      transferMoneyAndAccept(payment);
      log
          .debug(
              "Sucessfully transferred money for card payment {} with card {} to account {}",
              payment.getId(),
              card.getNumber(),
              destinationAccount.getIban());

      return ResponseEntity
          .created(
              URI
                  .create(
                      PAYMENT_PATH
                          .replace("{%s}".formatted(CARD_NUMBER_PLACEHOLDER), card.getNumber())
                          .replace(
                              "{%s}".formatted(PAYMENT_ID_PLACEHOLDER),
                              payment.getId().toString())))
          .build();

    } catch (HttpClientErrorException e) {
      if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
        log
            .warn(
                "Card payment with card {} to account {} rejected because the destination account is not known to the account service",
                card.getNumber(),
                dto.destinationIban());
        throw new ResourceNotFoundException(
            "Destination account %s is not known to the account-service"
                .formatted(dto.destinationIban()));
      }
      log
          .error(
              "Error while checking destination account {} existence for card payment with card {}",
              dto.destinationIban(),
              card.getNumber(),
              e);
      throw e;
    }
  }

  @Transactional(readOnly = true)
  Long getAcceptedPaymentsCumulatedAmountOn30Days(Card card) {
    final var now = Instant.now();
    final var last30DaysPayments = paymentRepo
        .findByCardNumberAndTimestampBetween(card.getNumber(), now.minus(30, ChronoUnit.DAYS), now);
    return last30DaysPayments
        .stream()
        .filter(CardPayment::isAccepted)
        .mapToLong(p -> p.getAmount().getDigits())
        .sum();

  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  CardPayment createPayemnt(Card card, CardPaymentCreationRequest dto) {
    return paymentRepo
        .save(
            CardPayment
                .builder()
                .amount(Amount.builder().currencyIso3(dto.currency()).digits(dto.amount()).build())
                .card(card)
                .destinationIban(Iban.of(dto.destinationIban()))
                .accepted(false)
                .build());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  CardPayment transferMoneyAndAccept(CardPayment payment) throws InternalServerErrorException {
    try {
      transfersApi
          .transferMoneyBetweenAccounts(
              new MoneyTransferRequest()
                  .amount(payment.getAmount().getDigits())
                  .currency(payment.getAmount().getCurrencyIso3())
                  .sourceIban(payment.getCard().getIban().toMachineReadableString())
                  .destinationIban(payment.getDestinationIban().toMachineReadableString())
                  .label(
                      "Payment with card %s to %s"
                          .formatted(
                              payment.getCard().getNumber(),
                              payment.getDestinationIban().toHumanReadableString())));
    } catch (HttpClientErrorException e) {
      log
          .error(
              "Error while transferring money for card payment {} with card {} to account {}: {}",
              payment.getId(),
              payment.getCard().getNumber(),
              payment.getDestinationIban(),
              e.getMessage());
      throw new InternalServerErrorException(
          "Error while transferring money: %s".formatted(e.getMessage()));
    }
    log
        .debug(
            "Sucessfully transferred money for card payment {} with card {} to account {}",
            payment.getId(),
            payment.getCard().getNumber(),
            payment.getDestinationIban());
    payment.setAccepted(true);
    final var acceptedPayment = paymentRepo.save(payment);
    log.debug("Saved card payment {} as accepted", acceptedPayment.getId());
    return acceptedPayment;
  }

}
