package nc.sgcb.labs.account;

import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.account.domain.MoneyTransfer;
import nc.sgcb.labs.commons.domain.Amount;

public class MoneyTransferFixtures {

  public static MoneyTransfer createMoneyTransfer(
      Account source,
      Account destination,
      Integer amountDigits) {
    return MoneyTransfer
        .builder()
        .amount(
            Amount
                .builder()
                .currency(source.getBalance().getCurrency())
                .digits(amountDigits)
                .build())
        .destinationIban(destination.getIban())
        .label(
            "Test transfer of %d %s from %s to %s"
                .formatted(
                    amountDigits,
                    source.getBalance().getCurrency(),
                    source.getIban(),
                    destination.getIban()))
        .sourceIban(source.getIban())
        .build();
  }
}
