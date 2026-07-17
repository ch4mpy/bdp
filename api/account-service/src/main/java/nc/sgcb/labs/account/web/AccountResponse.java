package nc.sgcb.labs.account.web;

public record AccountResponse(String iban, String customerId, String currency, Long balance) {

}
