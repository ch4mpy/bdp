package nc.sgcb.labs.account;

import nc.sgcb.labs.account.domain.Account;
import nc.sgcb.labs.commons.domain.Amount;
import nc.sgcb.labs.commons.domain.Iban;

public class Fixtures {

  public static String CUSTOMER_SUBJECT = "customer-subject";

  public static String SOMEONE_SUBJECT = "someone-subject";

  public static Account createCustomersXpfAccount(Long balanceDigits) {
    final var balance = Amount.builder().currencyIso3("XPF").digits(balanceDigits).build();
    return Account
        .builder()
        .customerId(CUSTOMER_SUBJECT)
        .iban(Iban.parse("FR761111222233334441"))
        .balance(balance)
        .build();
  }

  public static Account createCustomersEurAccount(Long balanceDigits) {
    final var balance = Amount.builder().currencyIso3("EUR").digits(balanceDigits).build();
    return Account
        .builder()
        .customerId(CUSTOMER_SUBJECT)
        .iban(Iban.parse("FR761111222233334442"))
        .balance(balance)
        .build();
  }

  public static Account createSomeonesXpfAccount(Long balanceDigits) {
    final var balance = Amount.builder().currencyIso3("XPF").digits(balanceDigits).build();
    return Account
        .builder()
        .customerId(SOMEONE_SUBJECT)
        .iban(Iban.parse("FR761111222233334443"))
        .balance(balance)
        .build();
  }
}
