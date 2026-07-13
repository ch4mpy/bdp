package nc.sgcb.labs.card.payment.web;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.account.api.AccountsApi;
import nc.sgcb.labs.card.payment.domain.Card;
import nc.sgcb.labs.card.payment.domain.Card.Ceilings;
import nc.sgcb.labs.card.payment.domain.CardService;
import nc.sgcb.labs.commons.domain.Iban;

@Tag(name = "Cards")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
@RequiredArgsConstructor
public class CardController {
  public static final String BASE_PATH = "/cards";
  public static final String CARD_NUMBER_PLACEHOLDER = "cardNumber";
  public static final String CARD_PATH = BASE_PATH + "/{" + CARD_NUMBER_PLACEHOLDER + "}";
  public static final String CARD_STATUS_PATH = CARD_PATH + "/status";
  public static final String CARD_CEILINGS_PATH = CARD_PATH + "/ceilings";

  private final CardService cardService;
  private final CardMapper cardMapper;
  private final AccountsApi accountsApi;

  @GetMapping(path = BASE_PATH)
  public List<CardResponse> listCards(@RequestParam Iban iban) {
    var cards = cardService.findByIban(iban);
    return cards.stream().map(cardMapper::map).toList();
  }

  @PostMapping(path = BASE_PATH)
  public ResponseEntity<Void> createCard(@RequestBody @Valid CardCreationRequest dto) {
    // Check that the account is known by the account service
    var account = accountsApi.getAccount(dto.iban());
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
                    CARD_PATH
                        .replaceAll("{%s}".formatted(CARD_NUMBER_PLACEHOLDER), card.getNumber())))
        .build();
  }

  @GetMapping(path = CARD_PATH)
  public CardResponse getCard(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card) {
    return cardMapper.map(card);
  }

  @PutMapping(path = CARD_STATUS_PATH)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void setCardStatus(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @RequestBody CardStatusRequest dto) {
    card.setActive(dto.isActive());
    cardService.save(card);
  }

  @PutMapping(path = CARD_CEILINGS_PATH)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void setCardCeilings(
      @Parameter(schema = @Schema(type = "string"))
      @PathVariable(name = CARD_NUMBER_PLACEHOLDER) Card card,
      @RequestBody CardStatusRequest dto) {
    card.setActive(dto.isActive());
    cardService.save(card);
  }

}
