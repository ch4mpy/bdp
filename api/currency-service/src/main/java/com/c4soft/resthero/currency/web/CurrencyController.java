package com.c4soft.resthero.currency.web;

import java.util.Arrays;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.c4soft.resthero.commons.domain.Amount;
import com.c4soft.resthero.commons.domain.Currency;
import com.c4soft.resthero.commons.validation.SupportedCurrency;
import com.c4soft.resthero.currency.domain.ForexService;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Currencies")
@RestController
@RequestMapping(
    produces = {MediaType.APPLICATION_PROBLEM_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
@Observed
@Slf4j
public class CurrencyController {
  public static final String BASE_PATH = "/currencies";

  private final CurrencyMapper currencyMapper;

  private final ForexService forexService;

  @Transactional(readOnly = true)
  @GetMapping(BASE_PATH)
  @PreAuthorize("isAuthenticated()")
  public List<CurrencyResponse> listSupportedCurrencies() {

    return Arrays.stream(Currency.values()).map(currencyMapper::map).toList();
  }

  /**
   * 
   * @param digits amount in the source currency's smallest unit (e.g., cents for USD)
   * @param fromIso3 source currency ISO 4217 code
   * @param toIso3 target currency ISO 4217 code
   * @return the amount in the target currency's smallest unit
   */
  @GetMapping(BASE_PATH + "/change")
  @PreAuthorize("isAuthenticated()")
  public int change(
      @RequestParam int digits,
      @RequestParam @SupportedCurrency String fromIso3,
      @RequestParam @SupportedCurrency String toIso3) {
    Currency fromCurrency = Currency.valueOf(fromIso3);
    Currency toCurrency = Currency.valueOf(toIso3);

    return forexService.convert(new Amount(digits, fromCurrency), toCurrency).getDigits();
  }

}
