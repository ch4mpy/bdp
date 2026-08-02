package com.c4soft.resthero.account;

import com.c4soft.resthero.account.domain.Account;
import com.c4soft.resthero.commons.domain.Amount;
import com.c4soft.resthero.commons.domain.Currency;
import com.c4soft.resthero.commons.domain.Iban;

public class AccountFixtures {

  public static String CUSTOMER_SUBJECT = "customer-subject";

  public static String SOMEONE_SUBJECT = "someone-subject";

  public static Account createCustomersXpfAccount(Integer balanceDigits) {
    final var balance = Amount.builder().currency(Currency.XPF).digits(balanceDigits).build();
    return Account
        .builder()
        .customerId(CUSTOMER_SUBJECT)
        .iban(Iban.of("FR761111222233334441"))
        .balance(balance)
        .build();
  }

  public static Account createCustomersEurAccount(Integer balanceDigits) {
    final var balance = Amount.builder().currency(Currency.EUR).digits(balanceDigits).build();
    return Account
        .builder()
        .customerId(CUSTOMER_SUBJECT)
        .iban(Iban.of("FR761111222233334442"))
        .balance(balance)
        .build();
  }

  public static Account createSomeonesXpfAccount(Integer balanceDigits) {
    final var balance = Amount.builder().currency(Currency.XPF).digits(balanceDigits).build();
    return Account
        .builder()
        .customerId(SOMEONE_SUBJECT)
        .iban(Iban.of("FR761111222233334443"))
        .balance(balance)
        .build();
  }
}
