package nc.sgcb.labs.commons.domain;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class IbanStringMapper {

  public static @Nullable String mapIbanToString(@Nullable Iban iban) {
    return iban == null ? null : iban.toMachineReadableString();
  }

  public static @Nullable Iban mapStringToIban(@Nullable String ibanStr) {
    return ibanStr == null ? null : Iban.parse(ibanStr);
  }

}
