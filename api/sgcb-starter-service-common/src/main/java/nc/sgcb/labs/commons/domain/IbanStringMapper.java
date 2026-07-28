package nc.sgcb.labs.commons.domain;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class IbanStringMapper {

  public @Nullable String map(@Nullable Iban iban) {
    return iban == null ? null : iban.toMachineReadableString();
  }

  public @Nullable Iban map(@Nullable String ibanStr) {
    return ibanStr == null ? null : Iban.of(ibanStr);
  }

}
