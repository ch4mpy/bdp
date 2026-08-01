package nc.sgcb.labs.currency.domain;

import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Currency;

public interface ForexService {

  Amount convert(Amount amount, Currency targetCurrency);

}
