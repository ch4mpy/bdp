package nc.sgcb.labs.commons.jpa;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import nc.sgcb.labs.commons.domain.Iban;
import nc.sgcb.labs.commons.domain.IbanStringMapper;

@Converter(autoApply = true)
@RequiredArgsConstructor
public class IbanStringAttributeConverter implements AttributeConverter<Iban, String> {
  private final IbanStringMapper ibanStringMapper;

  @Override
  public @Nullable String convertToDatabaseColumn(@Nullable Iban attribute) {
    return ibanStringMapper.map(attribute);
  }

  @Override
  public @Nullable Iban convertToEntityAttribute(@Nullable String dbData) {
    return ibanStringMapper.map(dbData);
  }

}
