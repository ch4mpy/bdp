package nc.sgcb.labs.currency.web;

import jakarta.validation.constraints.NotNull;

public record CurrencyResponse(@NotNull String iso3, short decimals) {

}
