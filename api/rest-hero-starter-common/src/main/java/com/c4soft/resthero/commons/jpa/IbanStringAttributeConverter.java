package com.c4soft.resthero.commons.jpa;

import org.jspecify.annotations.Nullable;
import com.c4soft.resthero.commons.domain.Iban;
import com.c4soft.resthero.commons.domain.IbanStringMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

// LAB:3.4:REMOVE:START
@Converter(autoApply = true)
// LAB:3.4:REMOVE:END
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
