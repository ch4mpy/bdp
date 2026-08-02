package com.c4soft.resthero.currency.domain;

import com.c4soft.resthero.commons.domain.Amount;
import com.c4soft.resthero.commons.domain.Currency;

public interface ForexService {

  Amount convert(Amount amount, Currency targetCurrency);

}
