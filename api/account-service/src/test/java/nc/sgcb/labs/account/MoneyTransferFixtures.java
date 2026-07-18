package nc.sgcb.labs.account;

import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.commons.domain.Amount;

public class MoneyTransferFixtures {

  public static MoneyTransfer createMoneyTransfer(
      Account source,
      Account destination,
      Long amountDigits) {
    return MoneyTransfer
        .builder()
        .amount(
            Amount
                .builder()
                .currencyIso3(source.getBalance().getCurrencyIso3())
                .digits(amountDigits)
                .build())
        .destinationIban(destination.getIban())
        .label(
            "Test transfer of %d %s from %s to %s"
                .formatted(
                    amountDigits,
                    source.getBalance().getCurrencyIso3(),
                    source.getIban(),
                    destination.getIban()))
        .sourceIban(source.getIban())
        .build();
  }
}
