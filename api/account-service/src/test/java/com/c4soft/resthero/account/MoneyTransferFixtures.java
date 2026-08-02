package com.c4soft.resthero.account;

import com.c4soft.resthero.account.domain.Account;
import com.c4soft.resthero.account.domain.MoneyTransfer;
import com.c4soft.resthero.commons.domain.Amount;

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
