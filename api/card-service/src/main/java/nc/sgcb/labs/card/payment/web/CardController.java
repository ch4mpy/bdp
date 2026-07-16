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
import nc.sgcb.labs.card.payment.domain.CardService;
import nc.sgcb.labs.card.payment.jpa.CardPaymentJpaRepository;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.domain.Period;
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

  private final CardService cardService;
  private final CardMapper cardMapper;
  private final CardPaymentJpaRepository paymentRepo;
  private final CardPaymentMapper paymentMapper;
  private final AccountsApi accountsApi;
  private final MoneyTransfersApi transfersApi;

  @Transactional(readOnly = true)
  @GetMapping(path = BASE_PATH)
  public List<CardResponse> listCards(@RequestParam Iban iban) {
    var cards = cardService.findByIban(iban);
    return cards.stream().map(cardMapper::map).toList();
  }

  @Transactional
  @PostMapping(path = BASE_PATH)
  public ResponseEntity<Void> createCard(@RequestBody @Valid CardCreationRequest dto)
      throws ResourceNotFoundException {
    // Assert that the account is known by the account service
    try {
      accountsApi.getAccount(dto.iban());
    } catch (HttpClientErrorException e) {
      if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
        throw new ResourceNotFoundException(
            "%s account is not known to the account-service".formatted(dto.iban()));
      }
      throw e;
    }
    var iban = Iban.parse(dto.iban());
    var existingCards = cardService.findByIban(iban);
    var cardNumber = "4%s%d".formatted(iban.getBban(), existingCards.size());

    var card = cardService
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

    return ResponseEntity
        .created(
            URI
                .create(
                    CARD_PATH.replace("{%s}".formatted(CARD_NUMBER_PLACEHOLDER), card.getNumber())))
        .build();
  }

  @Transactional(readOnly = true)
  @GetMapping(path = CARD_PATH)
  public CardResponse getCard(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card) {
    return cardMapper.map(card);
  }

  @Transactional
  @PutMapping(path = CARD_STATUS_PATH)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void setCardStatus(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @RequestBody CardStatusRequest dto) {
    card.setActive(dto.isActive());
    cardService.save(card);
  }

  @Transactional
  @PutMapping(path = CARD_CEILINGS_PATH)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void setCardCeilings(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @RequestBody CardStatusRequest dto) {
    card.setActive(dto.isActive());
    cardService.save(card);
  }

  @Transactional(readOnly = true)
  @GetMapping(path = PAYMENT_LIST_PATH)
  public List<CardPaymentResponse> listCardPayments(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @ParameterObject @NotNull @ValidPeriod(maxSeconds = 60 * 24 * 3600) Period period) {
    return paymentRepo
        .findByCardNumberAndTimestampBetween(card.getNumber(), period.from(), period.to())
        .stream()
        .map(paymentMapper::map)
        .toList();
  }

  @PostMapping(path = PAYMENT_LIST_PATH)
  public ResponseEntity<Void> createCardPayment(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @RequestBody @Valid CardPaymentCreationRequest dto) throws ResourceNotFoundException {
    // Assert that the destination account is known by the account service
    try {
      final var account = accountsApi.getAccount(dto.destIban()).getBody();
      var payment = createPayemnt(card, dto);

      if (Objects.equals(account.getCurrency(), dto.currency())) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Card payments are currently accepted only in the %s account's currency: %s"
                .formatted(account.getIban(), account.getCurrency()));
      }

      if (card.getCeilings().getTransaction().compareTo(dto.amount()) < 0) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Card transaction ceiling set at %d does not allow this payment of %d"
                .formatted(card.getCeilings().getTransaction(), dto.amount()));
      }

      final var cumulatedAmount = getAcceptedPaymentsCumulatedAmountOn30Days(card);
      if (card.getCeilings().getRolling30().compareTo(cumulatedAmount + dto.amount()) < 0) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Card rolling30 ceiling set at %d does not allow this payment of %d because the cumulated amount of accepted payments is %d"
                .formatted(card.getCeilings().getRolling30(), dto.amount(), cumulatedAmount));
      }

      transferMoneyAndAccept(payment);

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
        throw new ResourceNotFoundException(
            "Destination account %s is not known to the account-service".formatted(dto.destIban()));
      }
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
        .filter(CardPayment::getIsAccepted)
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
                .destinationIban(Iban.parse(dto.destIban()))
                .isAccepted(false)
                .build());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  CardPayment transferMoneyAndAccept(CardPayment payment) {
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
    payment.setIsAccepted(true);
    return paymentRepo.save(payment);
  }

}
