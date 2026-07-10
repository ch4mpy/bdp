package nc.sgcb.labs.account.web;

import java.time.Instant;

/**
 * @param amount In minor unit (i.e. 1000 for 1000 XPF, 10.00 USD, 1.000 KWD)
 * @param currency in ISO_3 format
 */
public record MoneyTransferResponse(String fromIban, String toIban, Long amount,
    String currency, Instant timestamp, String label) {

}
