package nc.sgcb.labs.commons.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.domain.IbanStringMapper;
import org.jspecify.annotations.Nullable;

@Converter(autoApply = true)
public class IbanStringAttributeConverter implements AttributeConverter<Iban, String> {

  @Override
  public @Nullable String convertToDatabaseColumn(@Nullable Iban attribute) {
    return IbanStringMapper.mapIbanToString(attribute);
  }

  @Override
  public @Nullable Iban convertToEntityAttribute(@Nullable String dbData) {
    return IbanStringMapper.mapStringToIban(dbData);
  }

}
