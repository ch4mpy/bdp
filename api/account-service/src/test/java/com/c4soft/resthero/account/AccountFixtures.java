package com.c4soft.resthero.account;

import com.c4soft.resthero.account.domain.Account;
import com.c4soft.resthero.commons.domain.Amount;
import com.c4soft.resthero.commons.domain.Currency;
import com.c4soft.resthero.commons.domain.Iban;

public class AccountFixtures {

  public static String CUSTOMER_SUBJECT = "customer-subject";

  public static String SOMEONE_SUBJECT = "someone-subject";

  public static Account createCustomersXpfAccount(Integer balanceDigits) {
    final var account =
        Account.create(Iban.of("FR761111222233334441"), CUSTOMER_SUBJECT, Currency.XPF);
    account.credit(new Amount(balanceDigits, Currency.XPF));
    return account;
  }

  public static Account createCustomersEurAccount(Integer balanceDigits) {
    final var account =
        Account.create(Iban.of("FR761111222233334442"), CUSTOMER_SUBJECT, Currency.EUR);
    account.credit(new Amount(balanceDigits, Currency.EUR));
    return account;
  }

  public static Account createSomeonesXpfAccount(Integer balanceDigits) {
    final var account =
        Account.create(Iban.of("FR761111222233334443"), SOMEONE_SUBJECT, Currency.XPF);
    account.credit(new Amount(balanceDigits, Currency.XPF));
    return account;
  }
}
