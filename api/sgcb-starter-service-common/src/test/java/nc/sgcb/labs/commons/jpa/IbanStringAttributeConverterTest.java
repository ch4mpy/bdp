package nc.sgcb.labs.commons.jpa;

import nc.sgcb.labs.commons.domain.Iban;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IbanStringAttributeConverterTest {

  IbanStringAttributeConverter converter = new IbanStringAttributeConverter();

  @Test
  @SuppressWarnings("null")
  void givenValidHumanReadableIbanString_whenConvertToEntityAttribute_thenProduceValidIban() {
    var ibanStr = "FR76 1111222233334444";
    var actual = converter.convertToEntityAttribute(ibanStr);
    assertEquals("FR", actual.getCountryCode());
    assertEquals("76", actual.getCheckDigits());
    assertEquals("1111222233334444", actual.getBban());
  }

  @Test
  @SuppressWarnings("null")
  void givenValidMachineReadableIbanString_whenConvertToEntityAttribute_thenProduceValidIban() {
    var ibanStr = "FR761111222233334444";
    var actual = converter.convertToEntityAttribute(ibanStr);
    assertEquals("FR", actual.getCountryCode());
    assertEquals("76", actual.getCheckDigits());
    assertEquals("1111222233334444", actual.getBban());
  }

  @Test
  void givenValidIban_whenConvertToconvertToDatabaseColumn_thenProduceHumanReadableString() {
    var iban = Iban.of("FR761111222233334444");
    assertEquals("FR761111222233334444", converter.convertToDatabaseColumn(iban));
  }

}
