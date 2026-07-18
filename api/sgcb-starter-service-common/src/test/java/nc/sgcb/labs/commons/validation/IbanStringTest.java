package nc.sgcb.labs.commons.validation;

import org.junit.jupiter.api.Test;
import nc.sgcb.labs.commons.validation.IbanString.IbanConstraintValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IbanStringTest {

  IbanConstraintValidator validator = new IbanConstraintValidator();

  @Test
  void givenValueIsNull_whenIsValid_thenTrue() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  void givenValueIsValidMachineReadableIban_whenIsValid_thenTrue() {
    assertTrue(validator.isValid("FR761111222233334444", null));
  }

  @Test
  void givenValueIsValidHumanReadableIban_whenIsValid_thenTrue() {
    assertTrue(validator.isValid("FR76 1111222233334444", null));
  }

  @Test
  void givenValueIsLowercaseIban_whenIsValid_thenTrue() {
    assertTrue(validator.isValid("fr761111222233334444", null));
  }

  @Test
  void givenValueDoesNotStartWithCountryCode_whenIsValid_thenFalse() {
    assertFalse(validator.isValid("1276111122223333444", null));
  }

  @Test
  void givenValueIsTooShort_whenIsValid_thenFalse() {
    assertFalse(validator.isValid("FR76", null));
  }

  @Test
  void givenValueIsEmpty_whenIsValid_thenFalse() {
    assertFalse(validator.isValid("", null));
  }

  @Test
  void givenValueContainsInvalidCharacters_whenIsValid_thenFalse() {
    assertFalse(validator.isValid("FR76-1111222233334444", null));
  }

}
